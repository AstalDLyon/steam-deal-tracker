package com.tracker.backend.service;

import com.tracker.backend.domain.HistoricoPreco;
import com.tracker.backend.domain.JogoMonitorado;
import com.tracker.backend.dto.itad.ItadDealDTO;
import com.tracker.backend.dto.itad.ItadGamePriceDTO;
import com.tracker.backend.dto.steam.SteamWishlistItemDTO;
import com.tracker.backend.repository.HistoricoPrecoRepository;
import com.tracker.backend.repository.JogoMonitoradoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromocaoService {

    private final SteamWishlistService steamWishlistService;
    private final IsThereAnyDealService isThereAnyDealService;
    private final JogoMonitoradoRepository jogoMonitoradoRepository;
    private final HistoricoPrecoRepository historicoPrecoRepository;

    private final java.util.concurrent.atomic.AtomicBoolean verificandoAgora =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    public List<JogoMonitorado> verificarPromocoes() {

        if (!verificandoAgora.compareAndSet(false, true)) {
            log.warn("Verificação já em andamento — ignorando chamada simultânea");
            return new ArrayList<>();
        }

        try{
            List<JogoMonitorado> novosMinimos = new ArrayList<>();

            List<SteamWishlistItemDTO> wishlist = steamWishlistService.buscarWishlist();
            log.info("Verificando promoções para {} jogos da wishlist", wishlist.size());

            if (wishlist.isEmpty()) return novosMinimos;

            List<Long> appIds = wishlist.stream()
                    .map(SteamWishlistItemDTO::appId)
                    .toList();

            sincronizarComWishlist(appIds);

            Map<String, String> lookupMap = isThereAnyDealService.resolverItadGameIds(appIds);

            List<String> itadIds = new ArrayList<>();
            List<Long> appIdsOrdenados = new ArrayList<>();

            for (Long appId : appIds) {
                String shopKey = "app/%d".formatted(appId);
                String itadId = lookupMap.get(shopKey);
                if (itadId != null) {
                    itadIds.add(itadId);
                    appIdsOrdenados.add(appId);
                } else {
                    log.debug("AppID {} não encontrado no ITAD", appId);
                }
            }

            if (itadIds.isEmpty()) {
                log.warn("Nenhum jogo da wishlist foi encontrado no ITAD");
                return novosMinimos;
            }

            List<ItadGamePriceDTO> precos = isThereAnyDealService.buscarPrecos(itadIds);

            for (int i = 0; i < precos.size(); i++) {
                ItadGamePriceDTO preco = precos.get(i);
                Long steamAppId = appIdsOrdenados.get(i);

                try {
                    JogoMonitorado resultado = processarJogo(steamAppId, preco);
                    if (resultado != null) {
                        novosMinimos.add(resultado);
                    }
                } catch (Exception e) {
                    log.error("Erro ao processar jogo AppID {}: {}", steamAppId, e.getMessage());
                }

            }

            log.info("Verificação concluída: {} novo(s) mínimo(s) histórico(s)", novosMinimos.size());
            return novosMinimos;
            
        }  finally {
            verificandoAgora.set(false);
        }

        }


    @Transactional
    public JogoMonitorado processarJogo(Long steamAppId, ItadGamePriceDTO precoDTO) {
        if (precoDTO.deals() == null || precoDTO.deals().isEmpty()) return null;

        // 1. Filtrar deal da Steam
        ItadDealDTO dealSteam = precoDTO.deals().stream()
                .filter(d -> d.shop() != null
                        && d.shop().id() != null
                        && d.shop().id() == 61)
                .findFirst()
                .orElse(null);

        if (dealSteam == null || dealSteam.precoAtual() == null) return null;

        // 2. Buscar ou criar jogo no banco
        JogoMonitorado jogo = jogoMonitoradoRepository
                .findBySteamAppId(steamAppId)
                .orElse(null);

        boolean jogoNovo = jogo == null;

        SteamWishlistService.DetalhesJogo detalhes =
                steamWishlistService.buscarDetalhesJogo(steamAppId);

        if (jogoNovo) {
            jogo = JogoMonitorado.builder()
                    .steamAppId(steamAppId)
                    .nome(detalhes.nome())
                    .build();
        }

// 4. Preço atual
        BigDecimal precoAtual = detalhes.precoAtual() != null
                ? detalhes.precoAtual()
                : dealSteam.precoAtual().valor();

        if (precoAtual == null) return null;


        Integer percentualDesconto = dealSteam.percentualDesconto() != null
                ? dealSteam.percentualDesconto() : 0;

        // 4. Atualizar preço atual e data
        jogo.setPrecoAtual(precoAtual);
        jogo.setUltimaVerificacao(LocalDateTime.now());

        // 5. Calcular mínimo histórico (três fontes, últimos 12 meses)
        BigDecimal minimoHistorico = calcularMinimoHistorico(steamAppId, jogo, dealSteam, precoDTO, precoAtual);

        if (minimoHistorico != null) {
            jogo.setMenorPrecoHistorico(minimoHistorico);
        }

        // 6. Salvar jogo ANTES do histórico
        jogo = jogoMonitoradoRepository.save(jogo);

        // 7. Registrar histórico só quando há desconto
        if (percentualDesconto > 0) {
            HistoricoPreco historico = HistoricoPreco.builder()
                    .jogoMonitorado(jogo)
                    .preco(precoAtual)
                    .percentualDesconto(percentualDesconto)
                    .dataRegistro(LocalDateTime.now())
                    .build();
            historicoPrecoRepository.save(historico);
        }

        // 8. Verificar se é novo mínimo histórico
        boolean ehNovoMinimo = percentualDesconto > 0
                && minimoHistorico != null
                && precoAtual.compareTo(minimoHistorico) <= 0;

        if (ehNovoMinimo) {
            log.info("NOVO MÍNIMO HISTÓRICO NA STEAM: {} — R$ {}", steamAppId, precoAtual);
        }

        return ehNovoMinimo ? jogo : null;
    }

    private BigDecimal calcularMinimoHistorico(
            Long steamAppId,
            JogoMonitorado jogo,
            ItadDealDTO dealSteam,
            ItadGamePriceDTO precoDTO,
            BigDecimal precoAtual) {

        LocalDateTime dataLimite = LocalDateTime.now().minusMonths(12);
        BigDecimal minimo = null;

        // Fonte 1: storeLow do deal da Steam (ITAD)
        if (dealSteam.minimoHistoricoLoja() != null
                && dealSteam.minimoHistoricoLoja().valor() != null) {
            minimo = dealSteam.minimoHistoricoLoja().valor();
            log.debug("AppID {} — storeLow: {}", steamAppId, minimo);
        }

        // Fonte 2: historyLow.y1 do ITAD (último ano, todas as lojas)
        if (precoDTO.historyLow() != null
                && precoDTO.historyLow().ultimoAno() != null
                && precoDTO.historyLow().ultimoAno().valor() != null) {
            BigDecimal y1 = precoDTO.historyLow().ultimoAno().valor();
            log.debug("AppID {} — historyLow.y1: {}", steamAppId, y1);
            if (minimo == null || y1.compareTo(minimo) < 0) {
                minimo = y1;
            }
        }

        // Fonte 3: menor preço no banco dos últimos 12 meses
        if (jogo.getId() != null) {
            Optional<BigDecimal> minBanco = historicoPrecoRepository
                    .findMenorPrecoUltimos12Meses(jogo.getId(), dataLimite);
            if (minBanco.isPresent()) {
                log.debug("AppID {} — minBanco 12m: {}", steamAppId, minBanco.get());
                if (minimo == null || minBanco.get().compareTo(minimo) < 0) {
                    minimo = minBanco.get();
                }
            }
        }

        // Fonte 4: preço atual — sempre inclui na comparação
        if (precoAtual != null) {
            if (minimo == null || precoAtual.compareTo(minimo) < 0) {
                minimo = precoAtual;
            }
        }

        log.debug("AppID {} — mínimo final: {}", steamAppId, minimo);
        return minimo;
    }

    @Transactional
    public void sincronizarComWishlist(List<Long> appIdsAtuais) {
        List<JogoMonitorado> jogosNoBanco = jogoMonitoradoRepository.findAll();

        List<JogoMonitorado> jogosRemovidos = jogosNoBanco.stream()
                .filter(j -> !appIdsAtuais.contains(j.getSteamAppId()))
                .toList();

        if (!jogosRemovidos.isEmpty()) {
            log.info("Removendo {} jogo(s) que saíram da wishlist", jogosRemovidos.size());
            for (JogoMonitorado jogo : jogosRemovidos) {
                historicoPrecoRepository.deleteByJogoMonitoradoId(jogo.getId());
                jogoMonitoradoRepository.deleteBySteamAppId(jogo.getSteamAppId());
                log.info("Jogo removido: {} (AppID {})", jogo.getNome(), jogo.getSteamAppId());
            }
        }
    }
}
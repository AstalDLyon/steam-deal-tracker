package com.tracker.backend.service;

import com.tracker.backend.dto.itad.ItadGamePriceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IsThereAnyDealService {

    private static final String BASE_URL = "https://api.isthereanydeal.com";
    private static final int TAMANHO_LOTE = 200;

    private final RestClient restClient;
    private final ConfigService configService;

    public Map<String, String> resolverItadGameIds(List<Long> steamAppIds) {
        String apiKey = configService.getItadApiKey();
        String url = "%s/lookup/id/shop/61/v1?key=%s".formatted(BASE_URL, apiKey);

        List<String> shopGameIds = steamAppIds.stream()
                .map(id -> "app/%d".formatted(id))
                .toList();

        Map<String, String> resultado = new java.util.HashMap<>();

        for (int i = 0; i < shopGameIds.size(); i += TAMANHO_LOTE) {
            List<String> lote = shopGameIds.subList(i,
                    Math.min(i + TAMANHO_LOTE, shopGameIds.size()));

            log.info("Resolvendo IDs — lote {}/{} ({} jogos)",
                    (i / TAMANHO_LOTE) + 1,
                    (int) Math.ceil((double) shopGameIds.size() / TAMANHO_LOTE),
                    lote.size());

            Map<String, String> loteResposta = restClient.post()
                    .uri(url)
                    .body(lote)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, String>>() {});

            if (loteResposta != null) resultado.putAll(loteResposta);
        }

        long encontrados = resultado.values().stream().filter(v -> v != null).count();
        log.info("ITAD resolveu {}/{} jogos", encontrados, shopGameIds.size());
        return resultado;
    }

    public List<ItadGamePriceDTO> buscarPrecos(List<String> itadGameIds) {
        String apiKey = configService.getItadApiKey();
        String url = "%s/games/prices/v3?key=%s&country=BR".formatted(BASE_URL, apiKey);

        List<ItadGamePriceDTO> resultado = new ArrayList<>();

        for (int i = 0; i < itadGameIds.size(); i += TAMANHO_LOTE) {
            List<String> lote = itadGameIds.subList(i,
                    Math.min(i + TAMANHO_LOTE, itadGameIds.size()));

            log.info("Buscando preços — lote {}/{} ({} jogos)",
                    (i / TAMANHO_LOTE) + 1,
                    (int) Math.ceil((double) itadGameIds.size() / TAMANHO_LOTE),
                    lote.size());

            List<ItadGamePriceDTO> loteResposta = restClient.post()
                    .uri(url)
                    .body(lote)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (loteResposta != null) resultado.addAll(loteResposta);
        }

        log.info("Total de jogos com preço retornado: {}", resultado.size());
        return resultado;
    }
}

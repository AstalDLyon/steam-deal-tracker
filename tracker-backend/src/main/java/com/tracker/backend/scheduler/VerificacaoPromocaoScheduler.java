package com.tracker.backend.scheduler;

import com.tracker.backend.domain.JogoMonitorado;
import com.tracker.backend.service.PromocaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificacaoPromocaoScheduler {

    private final PromocaoService promocaoService;

    @Scheduled(fixedRateString = "${scheduler.verificacao.intervalo-ms}",
                initialDelayString = "${scheduler.verificacao.delay-inicial-ms}"
    )
    public void verificar() {
        log.info("Scheduler iniciado em {}", LocalDateTime.now());

        try {
            List<JogoMonitorado> novosMinimos = promocaoService.verificarPromocoes();

            if (novosMinimos.isEmpty()) {
                log.info("Scheduler concluído — nenhum novo mínimo histórico encontrado");
            } else {
                log.info("Scheduler concluído — {} novo(s) mínimo(s) histórico(s):", novosMinimos.size());
                novosMinimos.forEach(jogo ->
                        log.info("  → {} (AppID {}) — R$ {}",
                                jogo.getNome(), jogo.getSteamAppId(), jogo.getPrecoAtual())
                );
            }
        } catch (Exception e) {
            log.error("Erro durante verificação de promoções: {}", e.getMessage(), e);
        }
    }
}
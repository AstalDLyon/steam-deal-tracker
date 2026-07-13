package com.tracker.backend.dto;

import com.tracker.backend.domain.JogoMonitorado;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record JogoMonitoradoResponseDTO(
        Long id,
        Long steamAppId,
        String nome,
        BigDecimal precoAtual,
        BigDecimal menorPrecoHistorico,
        LocalDateTime ultimaVerificacao
) {
    // Método estático de fábrica — converte entidade para DTO
    public static JogoMonitoradoResponseDTO from(JogoMonitorado jogo) {
        return new JogoMonitoradoResponseDTO(
                jogo.getId(),
                jogo.getSteamAppId(),
                jogo.getNome(),
                jogo.getPrecoAtual(),
                jogo.getMenorPrecoHistorico(),
                jogo.getUltimaVerificacao()
        );
    }
}
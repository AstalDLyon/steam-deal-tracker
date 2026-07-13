package com.tracker.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "jogo_monitorado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JogoMonitorado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "steam_app_id", nullable = false, unique = true)
    private Long steamAppId;

    @Column(nullable = false)
    private String nome;

    @Column(name = "preco_atual", precision = 10, scale = 2)
    private BigDecimal precoAtual;

    @Column(name = "menor_preco_historico", precision = 10, scale = 2)
    private BigDecimal menorPrecoHistorico;

    @Column(name = "ultima_verificacao")
    private LocalDateTime ultimaVerificacao;
}
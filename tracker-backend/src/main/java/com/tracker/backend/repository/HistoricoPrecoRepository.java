package com.tracker.backend.repository;

import com.tracker.backend.domain.HistoricoPreco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HistoricoPrecoRepository extends JpaRepository<HistoricoPreco, Long> {

    List<HistoricoPreco> findByJogoMonitoradoIdOrderByDataRegistroDesc(Long jogoMonitoradoId);

    @Query("SELECT MIN(h.preco) FROM HistoricoPreco h " +
            "WHERE h.jogoMonitorado.id = :jogoId " +
            "AND h.dataRegistro >= :dataLimite")
    Optional<BigDecimal> findMenorPrecoUltimos12Meses(
            @Param("jogoId") Long jogoId,
            @Param("dataLimite") LocalDateTime dataLimite);

    @Modifying
    @Transactional
    void deleteByJogoMonitoradoId(Long jogoMonitoradoId);
}
package com.tracker.backend.repository;

import com.tracker.backend.domain.JogoMonitorado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface JogoMonitoradoRepository extends JpaRepository<JogoMonitorado, Long> {

    Optional<JogoMonitorado> findBySteamAppId(Long steamAppId);

    @Modifying
    @Transactional
    void deleteBySteamAppId(Long steamAppId);
}
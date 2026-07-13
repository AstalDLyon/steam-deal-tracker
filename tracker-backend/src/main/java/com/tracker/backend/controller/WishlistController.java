package com.tracker.backend.controller;

import com.tracker.backend.domain.JogoMonitorado;
import com.tracker.backend.repository.JogoMonitoradoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tracker.backend.dto.JogoMonitoradoResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final JogoMonitoradoRepository jogoMonitoradoRepository;

    @GetMapping
    public ResponseEntity<List<JogoMonitoradoResponseDTO>> listarJogosMonitorados() {
        List<JogoMonitoradoResponseDTO> jogos = jogoMonitoradoRepository.findAll()
                .stream()
                .map(JogoMonitoradoResponseDTO::from)
                .toList();
        return ResponseEntity.ok(jogos);
    }
}

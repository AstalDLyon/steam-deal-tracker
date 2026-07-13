package com.tracker.backend.controller;

import com.tracker.backend.dto.JogoMonitoradoResponseDTO;
import com.tracker.backend.service.PromocaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/promocoes")
@RequiredArgsConstructor
public class PromocaoController {

    private final PromocaoService promocaoService;
    private final AtomicBoolean verificandoAgora = new AtomicBoolean(false);

    @GetMapping("/verificar")
    public ResponseEntity<List<JogoMonitoradoResponseDTO>> verificarAgora() {
        List<JogoMonitoradoResponseDTO> novosMinimos = promocaoService.verificarPromocoes()
                .stream()
                .map(JogoMonitoradoResponseDTO::from)
                .toList();
        return ResponseEntity.ok(novosMinimos);
    }
}

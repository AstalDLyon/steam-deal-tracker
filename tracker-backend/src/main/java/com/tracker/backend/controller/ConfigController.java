package com.tracker.backend.controller;

import com.tracker.backend.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {

    @Value("${scheduler.verificacao.intervalo-ms}")
    private String intervaloMs;

    private final ConfigService configService;

    @GetMapping
    public ResponseEntity<Map<String, String>> getConfig() {
        return ResponseEntity.ok(Map.of(
                "intervaloVerificacaoMs", intervaloMs
        ));
    }

    @PostMapping
    public ResponseEntity<Void> salvarConfig(@RequestBody Map<String, String> config) {
        configService.salvar(
                config.get("steamId"),
                config.get("steamApiKey"),
                config.get("itadApiKey")
        );
        return ResponseEntity.ok().build();
    }
}

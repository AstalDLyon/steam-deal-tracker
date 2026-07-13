package com.tracker.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableScheduling
public class TrackerBackendApplication {

    public static void main(String[] args) {
        criarPastaDoUsuario();
        SpringApplication.run(TrackerBackendApplication.class, args);
    }

    private static void criarPastaDoUsuario() {
        Path pasta = Paths.get(System.getProperty("user.home"), "SteamDealTracker");
        if (!Files.exists(pasta)) {
            try {
                Files.createDirectories(pasta);
                System.out.println("Pasta criada: " + pasta);
            } catch (IOException e) {
                System.err.println("Erro ao criar pasta: " + e.getMessage());
            }
        }
    }
}

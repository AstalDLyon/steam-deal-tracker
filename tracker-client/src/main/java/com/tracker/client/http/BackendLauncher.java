package com.tracker.client.http;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class BackendLauncher {

    private static final Logger log = Logger.getLogger(BackendLauncher.class.getName());
    private static Process backendProcess;

    public static boolean iniciarBackend() {
        // Durante desenvolvimento no IntelliJ, o backend já está rodando
        // Só inicia o processo se o JAR existir na mesma pasta do executável
        Path jarPath = resolverCaminhoJar();

        if (jarPath == null || !Files.exists(jarPath)) {
            log.info("tracker-backend.jar não encontrado — assumindo backend já rodando (modo dev)");
            return true; // no IntelliJ, o backend já está rodando manualmente
        }

        try {
            String javaExe = ProcessHandle.current().info().command()
                    .orElse("java");

            ProcessBuilder pb = new ProcessBuilder(
                    javaExe,
                    "-jar",
                    jarPath.toAbsolutePath().toString()
            );

            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD); // silencia o output do backend

            backendProcess = pb.start();
            log.info("Backend iniciado — PID: " + backendProcess.pid());
            return true;

        } catch (IOException e) {
            log.severe("Erro ao iniciar backend: " + e.getMessage());
            return false;
        }
    }

    public static void pararBackend() {
        if (backendProcess != null && backendProcess.isAlive()) {
            backendProcess.destroy();
            log.info("Backend encerrado");
        }
    }

    private static Path resolverCaminhoJar() {
        // Procura o JAR na mesma pasta do executável atual
        try {
            Path pastaExecutavel = Paths.get(
                    BackendLauncher.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParent();

            if (pastaExecutavel != null) {
                return pastaExecutavel.resolve("tracker-backend.jar");
            }
        } catch (Exception e) {
            log.warning("Não foi possível resolver o caminho do JAR: " + e.getMessage());
        }
        return null;
    }
}
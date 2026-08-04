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
        Path jarPath = resolverCaminhoJar();

        if (jarPath == null || !Files.exists(jarPath)) {
            log.info("tracker-backend.jar não encontrado — modo dev, backend já rodando");
            return true;
        }

        try {
            Path javaExe = jarPath.getParent()
                    .getParent()
                    .resolve("runtime")
                    .resolve("bin")
                    .resolve("java.exe");

            String javaCmd = Files.exists(javaExe)
                    ? javaExe.toAbsolutePath().toString()
                    : "java";

            log.info("Iniciando backend com: " + javaCmd);

            ProcessBuilder pb = new ProcessBuilder(
                    javaCmd,
                    "--enable-native-access=ALL-UNNAMED",
                    "-jar",
                    jarPath.toAbsolutePath().toString()
            );

            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.directory(jarPath.getParent().toFile());

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
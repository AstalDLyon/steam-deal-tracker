package com.tracker.client;

import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import com.tracker.client.controller.MonitorController;
import com.tracker.client.http.BackendApiClient;
import com.tracker.client.http.BackendLauncher;

import javax.swing.*;
import java.util.logging.Logger;

public class ClientApplication {

    private static final Logger log = Logger.getLogger(ClientApplication.class.getName());

    public static void main(String[] args) {
        // 1. Tema dark
        FlatOneDarkIJTheme.setup();

        UIManager.put("defaultFont", new javax.swing.plaf.FontUIResource(
                "SansSerif", java.awt.Font.PLAIN, 13));

        // 2. Iniciar backend em background
        BackendLauncher.iniciarBackend();

        // 3. Garantir que o backend para quando o programa fechar
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            BackendLauncher.pararBackend();
        }));

        // 4. Aguardar backend ficar online (até 60 segundos)
        BackendApiClient apiClient = new BackendApiClient();
        boolean online = aguardarBackend(apiClient, 60);

        if (!online) {
            JOptionPane.showMessageDialog(null,
                    "Não foi possível iniciar o backend.\n" +
                            "Verifique se o Java está instalado corretamente.",
                    "Erro de inicialização",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        // 5. Iniciar o controller na EDT
        SwingUtilities.invokeLater(() -> {
            try {
                MonitorController controller = new MonitorController();
                controller.iniciar();
            } catch (Exception e) {
                log.severe("Erro fatal: " + e.getMessage());
                JOptionPane.showMessageDialog(null,
                        "Erro ao iniciar:\n" + e.getMessage(),
                        "Erro Fatal",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    private static boolean aguardarBackend(BackendApiClient apiClient, int timeoutSegundos) {
        log.info("Aguardando backend ficar online...");

        for (int i = 0; i < timeoutSegundos; i++) {
            if (apiClient.isBackendOnline()) {
                log.info("Backend online após " + i + " segundo(s)");
                return true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        log.severe("Backend não ficou online em " + timeoutSegundos + " segundos");
        return false;
    }
}

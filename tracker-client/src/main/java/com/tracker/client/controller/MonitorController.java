package com.tracker.client.controller;

import com.tracker.client.http.BackendApiClient;
import com.tracker.client.model.ConfigManager;
import com.tracker.client.tray.TrayManager;
import com.tracker.client.view.JanelaPrincipal;
import com.tracker.client.view.SetupWizard;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MonitorController {

    private static final Logger log = Logger.getLogger(MonitorController.class.getName());
    private static final int INTERVALO_VERIFICACAO_MINUTOS = 180; // 3 horas

    private final ConfigManager configManager;
    private final BackendApiClient apiClient;
    private final TrayManager trayManager;

    private JanelaPrincipal janelaPrincipal;
    private ScheduledExecutorService scheduler;

    public MonitorController() {
        this.configManager = new ConfigManager();
        this.apiClient = new BackendApiClient();
        this.trayManager = new TrayManager();
    }

    public void iniciar() {
        // 1. Verificar se o backend está online
        if (!apiClient.isBackendOnline()) {
            JOptionPane.showMessageDialog(null,
                    "Não foi possível conectar ao backend.\n" +
                            "Verifique se o tracker-backend está rodando.",
                    "Backend offline",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        // 2. Verificar se é a primeira execução
        if (!configManager.estaConfigurado()) {
            abrirSetupWizard();
            return;
        }

        // 3. Inicializar a bandeja e o scheduler
        inicializarTray();
        inicializarScheduler();

        log.info("Monitor iniciado com sucesso");
    }

    private void abrirSetupWizard() {
        SwingUtilities.invokeLater(() -> {
            SetupWizard wizard = new SetupWizard(configManager, apiClient);
            wizard.setVisible(true);

            if (wizard.isConfiguracaoSalva()) {
                inicializarTray();
                inicializarScheduler();
            } else {
                System.exit(0);
            }
        });
    }

    private void inicializarTray() {
        trayManager.inicializar(
                this::abrirJanelaPrincipal,
                this::verificarAgora,
                this::abrirConfiguracoes
        );
    }

    private void inicializarScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "monitor-scheduler");
            t.setDaemon(true);
            return t;
        });

        // Primeira verificação após 1 minuto, depois a cada 3 horas
        scheduler.scheduleAtFixedRate(
                this::verificarPromocoes,
                1,
                INTERVALO_VERIFICACAO_MINUTOS,
                TimeUnit.MINUTES
        );

        log.info("Scheduler iniciado — verificação a cada " +
                INTERVALO_VERIFICACAO_MINUTOS + " minutos");
    }

    private void verificarPromocoes() {
        log.info("Verificando promoções...");

        List<Map<String, Object>> novosMinimos = apiClient.verificarPromocoes();

        if (novosMinimos.isEmpty()) {
            log.info("Nenhum novo mínimo histórico encontrado");
            return;
        }

        log.info(novosMinimos.size() + " novo(s) mínimo(s) histórico(s) encontrado(s)");

        // Notificar via bandeja para cada jogo em novo mínimo
        for (Map<String, Object> jogo : novosMinimos) {
            String nome = (String) jogo.getOrDefault("nome", "Jogo desconhecido");
            Object preco = jogo.getOrDefault("precoAtual", "?");

            trayManager.exibirNotificacao(
                    "🎮 Novo mínimo histórico!",
                    nome + " — R$ " + preco
            );

            // Pequena pausa entre notificações para não empilhar
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Atualizar a janela se estiver aberta
        if (janelaPrincipal != null && janelaPrincipal.isVisible()) {
            SwingUtilities.invokeLater(() -> janelaPrincipal.atualizarLista());
        }
    }

    private volatile boolean verificandoAgora = false;

    private void verificarAgora() {
        if (verificandoAgora) {
            log.warning("Verificação já em andamento — ignorando clique duplo");
            return;
        }

        Thread t = new Thread(() -> {
            verificandoAgora = true;
            try {
                verificarPromocoes();
            } finally {
                verificandoAgora = false;
            }
        }, "verificacao-manual");
        t.setDaemon(true);
        t.start();
    }

    private void abrirJanelaPrincipal() {
        SwingUtilities.invokeLater(() -> {
            if (janelaPrincipal == null) {
                janelaPrincipal = new JanelaPrincipal(apiClient);
            }
            janelaPrincipal.setVisible(true);
            janelaPrincipal.toFront();
            janelaPrincipal.atualizarLista();
        });
    }

    private void abrirConfiguracoes() {
        SwingUtilities.invokeLater(() -> {
            SetupWizard wizard = new SetupWizard(configManager, apiClient);
            wizard.setVisible(true);
        });
    }


}

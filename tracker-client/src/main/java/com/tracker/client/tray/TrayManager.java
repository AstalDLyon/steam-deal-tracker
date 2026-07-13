package com.tracker.client.tray;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

public class TrayManager {

    private static final Logger log = Logger.getLogger(TrayManager.class.getName());

    private TrayIcon trayIcon;
    private final SystemTray systemTray;

    // Callbacks — o Controller vai definir o que cada ação faz
    private Runnable aoAbrirJanela;
    private Runnable aoVerificarAgora;
    private Runnable aoAbrirConfiguracoes;

    public TrayManager() {
        if (!SystemTray.isSupported()) {
            throw new UnsupportedOperationException("System tray não suportado neste sistema");
        }
        this.systemTray = SystemTray.getSystemTray();
    }

    public void inicializar(
            Runnable aoAbrirJanela,
            Runnable aoVerificarAgora,
            Runnable aoAbrirConfiguracoes) {

        this.aoAbrirJanela = aoAbrirJanela;
        this.aoVerificarAgora = aoVerificarAgora;
        this.aoAbrirConfiguracoes = aoAbrirConfiguracoes;

        Image icone = criarIcone();
        PopupMenu menu = criarMenu();

        trayIcon = new TrayIcon(icone, "Steam Deal Tracker", menu);
        trayIcon.setImageAutoSize(true);

        // Duplo clique no ícone abre a janela principal
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    aoAbrirJanela.run();
                }
            }
        });

        try {
            systemTray.add(trayIcon);
            log.info("TrayIcon adicionado à bandeja do sistema");
        } catch (AWTException e) {
            log.severe("Erro ao adicionar ícone à bandeja: " + e.getMessage());
        }
    }

    private Image criarIcone() {
        // Ícone simples gerado programaticamente — um círculo verde
        // Substituir por um arquivo .png real antes de distribuir
        int size = 16;
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(size, size,
                        java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(100, 200, 100));
        g.fillOval(1, 1, size - 2, size - 2);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        g.drawString("S", 4, 11);
        g.dispose();
        return img;
    }

    private PopupMenu criarMenu() {
        PopupMenu menu = new PopupMenu();

        MenuItem itemAbrir = new MenuItem("Abrir");
        itemAbrir.addActionListener(e -> aoAbrirJanela.run());

        MenuItem itemVerificar = new MenuItem("Verificar Agora");
        itemVerificar.addActionListener(e -> aoVerificarAgora.run());

        MenuItem itemConfiguracoes = new MenuItem("Configurações");
        itemConfiguracoes.addActionListener(e -> aoAbrirConfiguracoes.run());

        MenuItem itemSair = new MenuItem("Sair");
        itemSair.addActionListener(e -> {
            systemTray.remove(trayIcon);
            System.exit(0);
        });

        menu.add(itemAbrir);
        menu.add(itemVerificar);
        menu.add(itemConfiguracoes);
        menu.addSeparator();
        menu.add(itemSair);

        return menu;
    }

    public void exibirNotificacao(String titulo, String mensagem) {
        if (trayIcon != null) {
            trayIcon.displayMessage(
                    titulo,
                    mensagem,
                    TrayIcon.MessageType.INFO
            );
        }
    }

    public void remover() {
        if (trayIcon != null) {
            systemTray.remove(trayIcon);
        }
    }
}
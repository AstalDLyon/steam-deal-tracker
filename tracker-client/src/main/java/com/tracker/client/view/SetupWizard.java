package com.tracker.client.view;

import com.tracker.client.http.BackendApiClient;
import com.tracker.client.model.ConfigManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SetupWizard extends JDialog {

    private final ConfigManager configManager;
    private final BackendApiClient apiClient;

    private JTextField campSteamId;
    private JTextField campSteamApiKey;
    private JTextField campItadApiKey;
    private JButton btnSalvar;
    private JButton btnAjudaSteamId;
    private JButton btnAjudaSteamKey;
    private JButton btnAjudaItadKey;

    private boolean configuracaoSalva = false;

    public SetupWizard(ConfigManager configManager, BackendApiClient apiClient) {
        this.configManager = configManager;
        this.apiClient = apiClient;
        configurarJanela();
        construirInterface();
    }

    private void configurarJanela() {
        setTitle("Steam Deal Tracker — Configuração Inicial");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(520, 400);
        setLocationRelativeTo(null);
    }

    private void construirInterface() {
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Título
        JLabel titulo = new JLabel("Bem-vindo ao Steam Deal Tracker!");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitulo = new JLabel("Configure suas chaves de API para começar a monitorar promoções.");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitulo.setHorizontalAlignment(SwingConstants.CENTER);
        subtitulo.setForeground(Color.GRAY);

        JPanel painelTitulo = new JPanel(new GridLayout(2, 1, 0, 5));
        painelTitulo.add(titulo);
        painelTitulo.add(subtitulo);

        // Formulário
        JPanel painelForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Steam ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        painelForm.add(new JLabel("Steam ID:"), gbc);

        campSteamId = new JTextField(configManager.getSteamId(), 25);
        gbc.gridx = 1; gbc.weightx = 1.0;
        painelForm.add(campSteamId, gbc);

        btnAjudaSteamId = new JButton("?");
        btnAjudaSteamId.setPreferredSize(new Dimension(30, 25));
        btnAjudaSteamId.setToolTipText("Como encontrar meu Steam ID?");
        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(btnAjudaSteamId, gbc);

        // Steam API Key
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        painelForm.add(new JLabel("Steam API Key:"), gbc);

        campSteamApiKey = new JTextField(configManager.getSteamApiKey(), 25);
        gbc.gridx = 1; gbc.weightx = 1.0;
        painelForm.add(campSteamApiKey, gbc);

        btnAjudaSteamKey = new JButton("?");
        btnAjudaSteamKey.setPreferredSize(new Dimension(30, 25));
        btnAjudaSteamKey.setToolTipText("Como obter minha Steam API Key?");
        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(btnAjudaSteamKey, gbc);

        // ITAD API Key
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("ITAD API Key:"), gbc);

        campItadApiKey = new JTextField(configManager.getItadApiKey(), 25);
        gbc.gridx = 1; gbc.weightx = 1.0;
        painelForm.add(campItadApiKey, gbc);

        btnAjudaItadKey = new JButton("?");
        btnAjudaItadKey.setPreferredSize(new Dimension(30, 25));
        btnAjudaItadKey.setToolTipText("Como obter minha ITAD API Key?");
        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(btnAjudaItadKey, gbc);

        // Botão salvar
        btnSalvar = new JButton("Salvar e Começar");
        btnSalvar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnSalvar.setPreferredSize(new Dimension(200, 35));

        JPanel painelBotao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelBotao.add(btnSalvar);

        painelPrincipal.add(painelTitulo, BorderLayout.NORTH);
        painelPrincipal.add(painelForm, BorderLayout.CENTER);
        painelPrincipal.add(painelBotao, BorderLayout.SOUTH);

        setContentPane(painelPrincipal);

        configurarEventos();
    }

    private void configurarEventos() {
        btnSalvar.addActionListener(this::aoSalvar);

        btnAjudaSteamId.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Acesse: steamcommunity.com/id/SEU_USUARIO\n" +
                        "Clique com botão direito → Ver código fonte\n" +
                        "Procure por: \"steamid\":\"76561198...\"\n\n" +
                        "Ou use o site: steamid.io",
                "Como encontrar seu Steam ID",
                JOptionPane.INFORMATION_MESSAGE));

        btnAjudaSteamKey.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "1. Acesse: steamcommunity.com/dev/apikey\n" +
                        "2. Faça login com sua conta Steam\n" +
                        "3. Coloque qualquer nome de domínio (ex: localhost)\n" +
                        "4. Copie a chave gerada",
                "Como obter Steam API Key",
                JOptionPane.INFORMATION_MESSAGE));

        btnAjudaItadKey.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "1. Acesse: isthereanydeal.com/dev/app/\n" +
                        "2. Crie uma conta ou faça login\n" +
                        "3. Registre um novo aplicativo\n" +
                        "4. Copie a API Key gerada",
                "Como obter ITAD API Key",
                JOptionPane.INFORMATION_MESSAGE));
    }

    private void aoSalvar(ActionEvent e) {
        String steamId = campSteamId.getText().trim();
        String steamApiKey = campSteamApiKey.getText().trim();
        String itadApiKey = campItadApiKey.getText().trim();

        // Validação
        if (steamId.isBlank() || steamApiKey.isBlank() || itadApiKey.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Todos os campos são obrigatórios.",
                    "Campos incompletos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verificar se o backend está online
        if (!apiClient.isBackendOnline()) {
            JOptionPane.showMessageDialog(this,
                    "Não foi possível conectar ao backend.\n" +
                            "Verifique se o tracker-backend está rodando.",
                    "Backend offline",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Salvar localmente
        configManager.salvar(steamId, steamApiKey, itadApiKey);

        // Enviar para o backend
        boolean enviado = apiClient.salvarConfig(steamId, steamApiKey, itadApiKey);
        if (!enviado) {
            JOptionPane.showMessageDialog(this,
                    "Configurações salvas localmente, mas não foi possível\n" +
                            "enviar ao backend. O programa tentará novamente ao iniciar.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }

        configuracaoSalva = true;
        dispose();
    }

    public boolean isConfiguracaoSalva() {
        return configuracaoSalva;
    }
}
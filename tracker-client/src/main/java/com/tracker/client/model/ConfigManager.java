package com.tracker.client.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigManager {

    private static final Logger log = Logger.getLogger(ConfigManager.class.getName());
    private static final String PASTA = System.getProperty("user.home") + "/SteamDealTracker";
    private static final String ARQUIVO = PASTA + "/config.properties";

    private static final String KEY_STEAM_ID = "steam.id";
    private static final String KEY_STEAM_API_KEY = "steam.api-key";
    private static final String KEY_ITAD_API_KEY = "itad.api-key";

    private final Properties properties;

    public ConfigManager() {
        this.properties = new Properties();
        criarPastaSeNecessario();
        carregarConfiguracoes();
    }

    private void criarPastaSeNecessario() {
        Path pasta = Paths.get(PASTA);
        if (!Files.exists(pasta)) {
            try {
                Files.createDirectories(pasta);
                log.info("Pasta criada: " + PASTA);
            } catch (IOException e) {
                log.severe("Erro ao criar pasta: " + e.getMessage());
            }
        }
    }

    private void carregarConfiguracoes() {
        Path arquivo = Paths.get(ARQUIVO);
        if (!Files.exists(arquivo)) {
            log.info("Arquivo de configuração não encontrado — primeira execução");
            return;
        }

        try (FileInputStream fis = new FileInputStream(ARQUIVO)) {
            properties.load(fis);
            log.info("Configurações carregadas com sucesso");
        } catch (IOException e) {
            log.severe("Erro ao carregar configurações: " + e.getMessage());
        }
    }

    public void salvar(String steamId, String steamApiKey, String itadApiKey) {
        properties.setProperty(KEY_STEAM_ID, steamId);
        properties.setProperty(KEY_STEAM_API_KEY, steamApiKey);
        properties.setProperty(KEY_ITAD_API_KEY, itadApiKey);

        try (FileOutputStream fos = new FileOutputStream(ARQUIVO)) {
            properties.store(fos, "Steam Deal Tracker - Configurações");
            log.info("Configurações salvas com sucesso");
        } catch (IOException e) {
            log.severe("Erro ao salvar configurações: " + e.getMessage());
        }
    }

    public String getSteamId() {
        return properties.getProperty(KEY_STEAM_ID, "");
    }

    public String getSteamApiKey() {
        return properties.getProperty(KEY_STEAM_API_KEY, "");
    }

    public String getItadApiKey() {
        return properties.getProperty(KEY_ITAD_API_KEY, "");
    }

    public boolean estaConfigurado() {
        return !getSteamId().isBlank()
                && !getSteamApiKey().isBlank()
                && !getItadApiKey().isBlank();
    }
}

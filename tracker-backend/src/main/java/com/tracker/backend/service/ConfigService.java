package com.tracker.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@Slf4j
@Service
public class ConfigService {

    private static final String ARQUIVO = System.getProperty("user.home")
            + "/SteamDealTracker/backend.properties";

    public void salvar(String steamId, String steamApiKey, String itadApiKey) {
        Properties props = new Properties();
        props.setProperty("steam.id", steamId);
        props.setProperty("steam.api-key", steamApiKey);
        props.setProperty("itad.api-key", itadApiKey);

        try (FileOutputStream fos = new FileOutputStream(ARQUIVO)) {
            props.store(fos, "Steam Deal Tracker — Backend Config");
            log.info("Configurações salvas em {}", ARQUIVO);
        } catch (IOException e) {
            log.error("Erro ao salvar configurações: {}", e.getMessage());
        }
    }

    public String getSteamId() {
        return ler("steam.id");
    }

    public String getSteamApiKey() {
        return ler("steam.api-key");
    }

    public String getItadApiKey() {
        return ler("itad.api-key");
    }

    public boolean estaConfigurado() {
        return !getSteamId().isBlank()
                && !getSteamApiKey().isBlank()
                && !getItadApiKey().isBlank();
    }

    private String ler(String chave) {
        if (!Files.exists(Paths.get(ARQUIVO))) return "";
        try (FileInputStream fis = new FileInputStream(ARQUIVO)) {
            Properties props = new Properties();
            props.load(fis);
            return props.getProperty(chave, "");
        } catch (IOException e) {
            log.error("Erro ao ler configuração '{}': {}", chave, e.getMessage());
            return "";
        }
    }
}

package com.tracker.client.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BackendApiClient {

    private static final Logger log = Logger.getLogger(BackendApiClient.class.getName());
    private static final String BASE_URL = "http://localhost:8080";
    private static final MediaType JSON = MediaType.get("application/json");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BackendApiClient() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public boolean isBackendOnline() {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/actuator/health")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (IOException e) {
            log.warning("Backend não está acessível: " + e.getMessage());
            return false;
        }
    }

    public boolean salvarConfig(String steamId, String steamApiKey, String itadApiKey) {
        try {
            Map<String, String> config = Map.of(
                    "steamId", steamId,
                    "steamApiKey", steamApiKey,
                    "itadApiKey", itadApiKey
            );

            String json = objectMapper.writeValueAsString(config);

            Request request = new Request.Builder()
                    .url(BASE_URL + "/config")
                    .post(RequestBody.create(json, JSON))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (IOException e) {
            log.severe("Erro ao salvar configuração: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> buscarJogosMonitorados() {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/wishlist")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return Collections.emptyList();
                }

                String body = response.body().string();
                return objectMapper.readValue(body, new TypeReference<>() {});
            }
        } catch (IOException e) {
            log.severe("Erro ao buscar jogos monitorados: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> verificarPromocoes() {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/promocoes/verificar")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return Collections.emptyList();
                }

                String body = response.body().string();
                return objectMapper.readValue(body, new TypeReference<>() {});
            }
        } catch (IOException e) {
            log.severe("Erro ao verificar promoções: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
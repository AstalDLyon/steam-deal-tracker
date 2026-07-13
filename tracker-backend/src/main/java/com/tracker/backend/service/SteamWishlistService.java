package com.tracker.backend.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.tracker.backend.dto.steam.SteamWishlistItemDTO;
import com.tracker.backend.dto.steam.SteamWishlistResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SteamWishlistService {

    private final RestClient restClient;
    private final ConfigService configService;

    public List<SteamWishlistItemDTO> buscarWishlist() {
        String steamId = configService.getSteamId();

        if (steamId.isBlank()) {
            log.warn("Steam ID não configurado");
            return Collections.emptyList();
        }

        String url = "https://api.steampowered.com/IWishlistService/GetWishlist/v1/?key=%s&steamid=%s"
                .formatted(configService.getSteamApiKey(), steamId);

        log.info("Buscando wishlist via API oficial para o ID {}", steamId);

        SteamWishlistResponseDTO resposta = restClient.get()
                .uri(url)
                .retrieve()
                .body(SteamWishlistResponseDTO.class);

        if (resposta == null || resposta.response() == null || resposta.response().items() == null) {
            log.warn("Wishlist retornou vazia ou nula");
            return Collections.emptyList();
        }

        log.info("Wishlist retornada com {} jogos", resposta.response().items().size());
        return resposta.response().items();
    }

    public record DetalhesJogo(String nome, BigDecimal precoAtual) {}

    public DetalhesJogo buscarDetalhesJogo(Long steamAppId) {
        try {
            Thread.sleep(300);

            String url = "https://store.steampowered.com/api/appdetails?appids=%d&filters=basic,price_overview&cc=br"
                    .formatted(steamAppId);

            String resposta = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            if (resposta == null) return new DetalhesJogo("AppID " + steamAppId, null);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(resposta);
            JsonNode data = root.path(String.valueOf(steamAppId)).path("data");

            if (data.isMissingNode()) return new DetalhesJogo("AppID " + steamAppId, null);

            // Nome
            String nome = data.path("name").isMissingNode()
                    ? "AppID " + steamAppId
                    : data.path("name").asText();

            // Preço atual (em centavos → reais)
            BigDecimal precoAtual = null;
            JsonNode finalPrice = data.path("price_overview").path("final");
            if (!finalPrice.isMissingNode()) {
                precoAtual = BigDecimal.valueOf(finalPrice.asLong())
                        .divide(BigDecimal.valueOf(100));
            }

            return new DetalhesJogo(nome, precoAtual);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new DetalhesJogo("AppID " + steamAppId, null);
        } catch (Exception e) {
            log.warn("Não foi possível buscar detalhes para AppID {}: {}", steamAppId, e.getMessage());
            return new DetalhesJogo("AppID " + steamAppId, null);
        }
    }
}
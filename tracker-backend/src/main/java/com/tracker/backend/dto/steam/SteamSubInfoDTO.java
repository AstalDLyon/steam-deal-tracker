package com.tracker.backend.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SteamSubInfoDTO(
        @JsonProperty("price") Integer preco,
        @JsonProperty("discount_pct") Integer percentualDesconto
) {
}
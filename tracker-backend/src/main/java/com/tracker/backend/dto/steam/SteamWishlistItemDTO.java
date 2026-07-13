package com.tracker.backend.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SteamWishlistItemDTO(
        @JsonProperty("appid") Long appId,
        @JsonProperty("priority") Integer priority
) {}
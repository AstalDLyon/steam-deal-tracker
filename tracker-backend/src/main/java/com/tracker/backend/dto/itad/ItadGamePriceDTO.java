package com.tracker.backend.dto.itad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItadGamePriceDTO(
        @JsonProperty("id") String itadGameId,
        @JsonProperty("historyLow") ItadHistoryLowDTO historyLow,
        @JsonProperty("deals") List<ItadDealDTO> deals
) {
}

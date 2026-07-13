package com.tracker.backend.dto.itad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItadValorDTO(
        @JsonProperty("amount") BigDecimal valor,
        @JsonProperty("currency") String moeda
) {
}

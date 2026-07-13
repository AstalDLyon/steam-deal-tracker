package com.tracker.backend.dto.itad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItadHistoryLowDTO(
        @JsonProperty("all") ItadValorDTO todosOsTempos,
        @JsonProperty("y1") ItadValorDTO ultimoAno,
        @JsonProperty("m3") ItadValorDTO ultimosTresMeses
) {
}
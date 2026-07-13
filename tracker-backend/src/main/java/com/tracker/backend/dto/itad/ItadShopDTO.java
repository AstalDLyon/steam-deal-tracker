package com.tracker.backend.dto.itad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItadShopDTO(
        @JsonProperty("id") Integer id,
        @JsonProperty("name") String name
) {}

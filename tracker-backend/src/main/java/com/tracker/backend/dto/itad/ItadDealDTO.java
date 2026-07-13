package com.tracker.backend.dto.itad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItadDealDTO(
        @JsonProperty("shop") ItadShopDTO shop,
        @JsonProperty("price") ItadValorDTO precoAtual,
        @JsonProperty("cut") Integer percentualDesconto,
        @JsonProperty("storeLow") ItadValorDTO minimoHistoricoLoja

) {
}

package com.mercadopublico.mvp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MercadoPublicoLicitacionDTO(
    @JsonProperty("CodigoExterno") String codigoExterno,
    @JsonProperty("Nombre") String nombre,
    @JsonProperty("CodigoEstado") Integer codigoEstado,
    @JsonProperty("FechaCierre") String fechaCierre
) {}
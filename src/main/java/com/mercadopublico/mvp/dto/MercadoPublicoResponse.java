package com.mercadopublico.mvp.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MercadoPublicoResponse(
    @JsonProperty("Cantidad") int cantidad,
    @JsonProperty("FechaCreacion") String fechaCreacion,
    @JsonProperty("Version") String version,
    @JsonProperty("Listado") List<MercadoPublicoLicitacionDTO> listado
) {}
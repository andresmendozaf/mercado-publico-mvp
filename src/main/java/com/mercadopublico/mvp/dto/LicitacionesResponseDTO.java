package com.mercadopublico.mvp.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LicitacionesResponseDTO(
    @JsonProperty("Cantidad") Integer cantidad,
    @JsonProperty("FechaCreacion") String fechaCreacion,
    @JsonProperty("Listado") List<LicitacionApiDTO> listado
) {
    public record LicitacionApiDTO(
        @JsonProperty("CodigoExterno") String codigoExterno,
        @JsonProperty("Nombre") String nombre,
        @JsonProperty("CodigoEstado") Integer codigoEstado,
        @JsonProperty("FechaCierre") String fechaCierre
    ) {}
}
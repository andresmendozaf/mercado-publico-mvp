package com.mercadopublico.mvp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MercadoPublicoLicitacionDTO(
    @JsonProperty("CodigoExterno") String codigoExterno,
    @JsonProperty("Nombre") String nombre,
    @JsonProperty("CodigoEstado") Integer codigoEstado,
    @JsonProperty("FechaCierre") String fechaCierre,
    @JsonProperty("Descripcion") String descripcion,
    @JsonProperty("PresupuestoEstimado") Double presupuestoEstimado,
    @JsonProperty("OrganismoComprador") String organismoComprador,
    @JsonProperty("RutComprador") String rutComprador
) {}
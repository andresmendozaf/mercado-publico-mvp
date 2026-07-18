package com.mercadopublico.mvp.dto;

public record PostulacionDTO(
    Double montoPostulacion,
    String propuestaTecnica,
    Long licitacionId,
    Long proveedorId
) {}
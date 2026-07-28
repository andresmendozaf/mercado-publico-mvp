package com.mercadopublico.mvp.dto;

import com.mercadopublico.mvp.model.EstadoPostulacion;

public record PostulacionDTO(
    Long id,
    Double montoPostulacion,
    String propuestaTecnica,
    EstadoPostulacion estado,
    Long licitacionId,
    Long proveedorId
) {}
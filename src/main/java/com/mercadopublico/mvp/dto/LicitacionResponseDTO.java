package com.mercadopublico.mvp.dto;

import java.time.Instant;

import com.mercadopublico.mvp.model.EstadoLicitacion;

/**
 * Contrato de salida del API para una Licitación.
 * Desacopla la entidad JPA de la representación pública, evitando exponer
 * el modelo de persistencia y problemas de serialización lazy.
 */
public record LicitacionResponseDTO(
        Long id,
        String codigoExterno,
        String nombre,
        String descripcion,
        Double presupuestoEstimado,
        EstadoLicitacion estado,
        Instant fechaCierre,
        String organismoComprador,
        String rutComprador,
        Instant fechaCreacion,
        Instant fechaActualizacion
) {
}

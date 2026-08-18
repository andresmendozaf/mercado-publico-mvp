package com.mercadopublico.mvp.dto;

import java.time.Instant;

/**
 * Contrato de salida del API para un Usuario.
 * Expone solo los campos públicos, sin filtrar la entidad JPA.
 */
public record UsuarioResponseDTO(
        Long id,
        String runOId,
        String nombre,
        String email,
        String rol,
        Instant fechaCreacion
) {
}

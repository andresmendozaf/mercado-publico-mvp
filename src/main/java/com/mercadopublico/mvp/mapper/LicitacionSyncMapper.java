package com.mercadopublico.mvp.mapper;

import com.mercadopublico.mvp.dto.MercadoPublicoLicitacionDTO;
import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.Licitacion;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;

/**
 * Frontera de integración con la API de Mercado Público (ChileCompra).
 * Cualquier cambio en el formato o comportamiento de esa API debe quedar
 * contenido en esta clase, sin afectar el contrato de salida propio.
 */
@Slf4j
@Component
public class LicitacionSyncMapper {

    /**
     * Mapea un DTO de la API a una Entidad Licitacion, aplicando fallbacks seguros.
     */
    public void actualizarEntidadDesdeDto(MercadoPublicoLicitacionDTO dto, Licitacion licitacion) {
        String codigo = (dto.codigoExterno() != null && !dto.codigoExterno().isBlank())
                ? dto.codigoExterno()
                : "SIN-CODIGO";

        String nombre = (dto.nombre() != null && !dto.nombre().isBlank())
                ? dto.nombre()
                : "Licitación sin nombre provisto";

        licitacion.setCodigoExterno(codigo);
        licitacion.setNombre(nombre);

        // Campos opcionales (Respetan los nulos)
        licitacion.setDescripcion(dto.descripcion());
        licitacion.setPresupuestoEstimado(dto.presupuestoEstimado());
        licitacion.setOrganismoComprador(dto.organismoComprador());
        licitacion.setRutComprador(dto.rutComprador());

        // Estados y Fechas
        licitacion.setEstado(EstadoLicitacion.desdeCodigoApi(dto.codigoEstado()));
        licitacion.setFechaCierre(parsearFechaCierre(dto.fechaCierre()));
    }

    private Instant parsearFechaCierre(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(fechaStr)
                .atZone(ZoneId.of("America/Santiago"))
                .toInstant();
        } catch (DateTimeParseException e) {
            log.warn("Formato de fecha inválido recibido de la API: {}", fechaStr);
            return null;
        }
    }
}

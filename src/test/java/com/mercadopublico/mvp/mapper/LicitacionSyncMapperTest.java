package com.mercadopublico.mvp.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mercadopublico.mvp.dto.MercadoPublicoLicitacionDTO;
import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.Licitacion;

class LicitacionSyncMapperTest {

    private final LicitacionSyncMapper licitacionSyncMapper = new LicitacionSyncMapper();

    @Test
    @DisplayName("Debe mapear todos los campos del DTO externo a una entidad nueva")
    void actualizarEntidadDesdeDto_mapeaTodosLosCampos() {
        MercadoPublicoLicitacionDTO dto = new MercadoPublicoLicitacionDTO(
                "1234-56-78",
                "Adquisición de Servidores",
                5,
                "2026-08-30T15:00:00",
                "Descripción detallada",
                2500000.0,
                "Ministerio de Defensa",
                "98.765.432-1");

        Licitacion licitacion = new Licitacion();

        licitacionSyncMapper.actualizarEntidadDesdeDto(dto, licitacion);

        assertEquals("1234-56-78", licitacion.getCodigoExterno());
        assertEquals("Adquisición de Servidores", licitacion.getNombre());
        assertEquals("Descripción detallada", licitacion.getDescripcion());
        assertEquals(2500000.0, licitacion.getPresupuestoEstimado());
        assertEquals("Ministerio de Defensa", licitacion.getOrganismoComprador());
        assertEquals("98.765.432-1", licitacion.getRutComprador());
        assertEquals(EstadoLicitacion.PUBLICADA, licitacion.getEstado());

        Instant fechaEsperada = LocalDateTime.parse("2026-08-30T15:00:00")
                .atZone(ZoneId.of("America/Santiago"))
                .toInstant();
        assertEquals(fechaEsperada, licitacion.getFechaCierre());
    }

    @Test
    @DisplayName("Debe aplicar el fallback 'SIN-CODIGO' cuando el código externo viene nulo")
    void actualizarEntidadDesdeDto_codigoExternoNulo_aplicaFallback() {
        MercadoPublicoLicitacionDTO dto = new MercadoPublicoLicitacionDTO(
                null, "Licitación X", 5, null, null, null, null, null);

        Licitacion licitacion = new Licitacion();
        licitacionSyncMapper.actualizarEntidadDesdeDto(dto, licitacion);

        assertEquals("SIN-CODIGO", licitacion.getCodigoExterno());
    }

    @Test
    @DisplayName("Debe aplicar el fallback 'SIN-CODIGO' cuando el código externo viene en blanco")
    void actualizarEntidadDesdeDto_codigoExternoEnBlanco_aplicaFallback() {
        MercadoPublicoLicitacionDTO dto = new MercadoPublicoLicitacionDTO(
                "   ", "Licitación X", 5, null, null, null, null, null);

        Licitacion licitacion = new Licitacion();
        licitacionSyncMapper.actualizarEntidadDesdeDto(dto, licitacion);

        assertEquals("SIN-CODIGO", licitacion.getCodigoExterno());
    }

    @Test
    @DisplayName("Debe aplicar el fallback de nombre cuando el nombre viene nulo o en blanco")
    void actualizarEntidadDesdeDto_nombreNulo_aplicaFallback() {
        MercadoPublicoLicitacionDTO dto = new MercadoPublicoLicitacionDTO(
                "1234-56-78", null, 5, null, null, null, null, null);

        Licitacion licitacion = new Licitacion();
        licitacionSyncMapper.actualizarEntidadDesdeDto(dto, licitacion);

        assertEquals("Licitación sin nombre provisto", licitacion.getNombre());
    }

    @Test
    @DisplayName("Debe respetar los nulos en los campos opcionales, sin aplicarles fallback")
    void actualizarEntidadDesdeDto_camposOpcionalesNulos_seRespetan() {
        MercadoPublicoLicitacionDTO dto = new MercadoPublicoLicitacionDTO(
                "1234-56-78", "Licitación X", 5, null, null, null, null, null);

        Licitacion licitacion = new Licitacion();
        licitacionSyncMapper.actualizarEntidadDesdeDto(dto, licitacion);

        assertNull(licitacion.getDescripcion());
        assertNull(licitacion.getPresupuestoEstimado());
        assertNull(licitacion.getOrganismoComprador());
        assertNull(licitacion.getRutComprador());
        assertNull(licitacion.getFechaCierre());
    }

    @Test
    @DisplayName("Debe resolver el estado a OTRO cuando el código de estado es desconocido")
    void actualizarEntidadDesdeDto_codigoEstadoDesconocido_resuelveAOtro() {
        MercadoPublicoLicitacionDTO dto = new MercadoPublicoLicitacionDTO(
                "1234-56-78", "Licitación X", 999, null, null, null, null, null);

        Licitacion licitacion = new Licitacion();
        licitacionSyncMapper.actualizarEntidadDesdeDto(dto, licitacion);

        assertEquals(EstadoLicitacion.OTRO, licitacion.getEstado());
    }

    @Test
    @DisplayName("Debe ignorar una fecha de cierre con formato inválido y dejarla en null")
    void actualizarEntidadDesdeDto_fechaCierreInvalida_quedaNula() {
        MercadoPublicoLicitacionDTO dto = new MercadoPublicoLicitacionDTO(
                "1234-56-78", "Licitación X", 5, "no-es-una-fecha", null, null, null, null);

        Licitacion licitacion = new Licitacion();
        licitacionSyncMapper.actualizarEntidadDesdeDto(dto, licitacion);

        assertNull(licitacion.getFechaCierre());
    }

    @Test
    @DisplayName("Debe dejar la fecha de cierre en null cuando viene en blanco")
    void actualizarEntidadDesdeDto_fechaCierreEnBlanco_quedaNula() {
        MercadoPublicoLicitacionDTO dto = new MercadoPublicoLicitacionDTO(
                "1234-56-78", "Licitación X", 5, "   ", null, null, null, null);

        Licitacion licitacion = new Licitacion();
        licitacionSyncMapper.actualizarEntidadDesdeDto(dto, licitacion);

        assertNull(licitacion.getFechaCierre());
    }

    @Test
    @DisplayName("Debe actualizar una entidad existente sobrescribiendo sus valores previos")
    void actualizarEntidadDesdeDto_entidadExistente_sobrescribeValores() {
        Licitacion existente = new Licitacion();
        existente.setId(1L);
        existente.setCodigoExterno("1234-56-78");
        existente.setNombre("Nombre anterior");
        existente.setEstado(EstadoLicitacion.PUBLICADA);

        MercadoPublicoLicitacionDTO dto = new MercadoPublicoLicitacionDTO(
                "1234-56-78", "Nombre actualizado", 6, null, null, null, null, null);

        licitacionSyncMapper.actualizarEntidadDesdeDto(dto, existente);

        assertEquals(1L, existente.getId());
        assertEquals("Nombre actualizado", existente.getNombre());
        assertEquals(EstadoLicitacion.CERRADA, existente.getEstado());
    }
}

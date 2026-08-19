package com.mercadopublico.mvp.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mercadopublico.mvp.dto.LicitacionResponseDTO;
import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.Licitacion;

class LicitacionMapperTest {

    private final LicitacionMapper licitacionMapper = new LicitacionMapper();

    private Licitacion licitacion;

    @BeforeEach
    void setUp() {
        licitacion = new Licitacion();
        licitacion.setId(1L);
        licitacion.setCodigoExterno("1234-56-78");
        licitacion.setNombre("Adquisición de Servidores");
        licitacion.setDescripcion("Descripción detallada del requerimiento");
        licitacion.setPresupuestoEstimado(2500000.0);
        licitacion.setEstado(EstadoLicitacion.PUBLICADA);
        licitacion.setFechaCierre(Instant.parse("2026-08-30T15:00:00Z"));
        licitacion.setOrganismoComprador("Ministerio de Defensa");
        licitacion.setRutComprador("98.765.432-1");
        licitacion.setFechaCreacion(Instant.parse("2026-01-01T00:00:00Z"));
        licitacion.setFechaActualizacion(Instant.parse("2026-01-02T00:00:00Z"));
    }

    @Test
    @DisplayName("toResponseDTO debe mapear individualmente los 11 campos de la entidad al DTO")
    void toResponseDTO_mapeaTodosLosCampos() {
        LicitacionResponseDTO resultado = licitacionMapper.toResponseDTO(licitacion);

        assertEquals(licitacion.getId(), resultado.id());
        assertEquals(licitacion.getCodigoExterno(), resultado.codigoExterno());
        assertEquals(licitacion.getNombre(), resultado.nombre());
        assertEquals(licitacion.getDescripcion(), resultado.descripcion());
        assertEquals(licitacion.getPresupuestoEstimado(), resultado.presupuestoEstimado());
        assertEquals(licitacion.getEstado(), resultado.estado());
        assertEquals(licitacion.getFechaCierre(), resultado.fechaCierre());
        assertEquals(licitacion.getOrganismoComprador(), resultado.organismoComprador());
        assertEquals(licitacion.getRutComprador(), resultado.rutComprador());
        assertEquals(licitacion.getFechaCreacion(), resultado.fechaCreacion());
        assertEquals(licitacion.getFechaActualizacion(), resultado.fechaActualizacion());
    }

    @Test
    @DisplayName("toResponseDTOList debe mapear cada elemento de la lista preservando el orden")
    void toResponseDTOList_mapeaElementosPreservandoOrden() {
        Licitacion segundaLicitacion = new Licitacion();
        segundaLicitacion.setId(2L);
        segundaLicitacion.setCodigoExterno("2222-33-44");
        segundaLicitacion.setNombre("Adquisición de Software");
        segundaLicitacion.setDescripcion("Licencias de software corporativo");
        segundaLicitacion.setPresupuestoEstimado(500000.0);
        segundaLicitacion.setEstado(EstadoLicitacion.CERRADA);
        segundaLicitacion.setFechaCierre(Instant.parse("2026-09-15T12:00:00Z"));
        segundaLicitacion.setOrganismoComprador("Ministerio de Salud");
        segundaLicitacion.setRutComprador("11.222.333-4");
        segundaLicitacion.setFechaCreacion(Instant.parse("2026-02-01T00:00:00Z"));
        segundaLicitacion.setFechaActualizacion(Instant.parse("2026-02-02T00:00:00Z"));

        List<LicitacionResponseDTO> resultado = licitacionMapper.toResponseDTOList(List.of(licitacion, segundaLicitacion));

        assertEquals(2, resultado.size());

        assertEquals(licitacion.getId(), resultado.get(0).id());
        assertEquals(licitacion.getCodigoExterno(), resultado.get(0).codigoExterno());
        assertEquals(licitacion.getNombre(), resultado.get(0).nombre());

        assertEquals(segundaLicitacion.getId(), resultado.get(1).id());
        assertEquals(segundaLicitacion.getCodigoExterno(), resultado.get(1).codigoExterno());
        assertEquals(segundaLicitacion.getNombre(), resultado.get(1).nombre());
    }
}

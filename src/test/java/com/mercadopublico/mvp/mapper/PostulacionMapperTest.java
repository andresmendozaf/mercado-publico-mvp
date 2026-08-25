package com.mercadopublico.mvp.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mercadopublico.mvp.dto.PostulacionDTO;
import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.EstadoPostulacion;
import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.model.Postulacion;
import com.mercadopublico.mvp.model.Usuario;

class PostulacionMapperTest {

    private final PostulacionMapper postulacionMapper = new PostulacionMapper();

    private Licitacion licitacion;
    private Usuario proveedor;

    @BeforeEach
    void setUp() {
        licitacion = new Licitacion();
        licitacion.setId(1L);
        licitacion.setEstado(EstadoLicitacion.PUBLICADA);

        proveedor = new Usuario();
        proveedor.setId(1L);
        proveedor.setNombre("Empresa Ejemplo SpA");
    }

    @Test
    @DisplayName("toEntity con estado explícito en el DTO debe ignorarlo y forzar POR_ESTUDIAR")
    void toEntity_conEstadoExplicito_fuerzaPorEstudiar() {
        PostulacionDTO dto = new PostulacionDTO(
                null,
                1500000.0,
                "Propuesta de Desarrollo",
                EstadoPostulacion.GANADA,
                1L,
                1L
        );

        Postulacion resultado = postulacionMapper.toEntity(dto, licitacion, proveedor);

        assertEquals(dto.montoPostulacion(), resultado.getMontoPostulacion());
        assertEquals(dto.propuestaTecnica(), resultado.getPropuestaTecnica());
        assertEquals(EstadoPostulacion.POR_ESTUDIAR, resultado.getEstado());
        assertSame(licitacion, resultado.getLicitacion());
        assertSame(proveedor, resultado.getProveedor());
    }

    @Test
    @DisplayName("toEntity sin estado en el DTO debe asignar POR_ESTUDIAR por defecto")
    void toEntity_sinEstado_asignaPorEstudiar() {
        PostulacionDTO dto = new PostulacionDTO(
                null,
                1000000.0,
                "Propuesta sin estado",
                null,
                1L,
                1L
        );

        Postulacion resultado = postulacionMapper.toEntity(dto, licitacion, proveedor);

        assertEquals(EstadoPostulacion.POR_ESTUDIAR, resultado.getEstado());
    }

    @Test
    @DisplayName("toEntity con DTO mínimo (sin monto, propuesta ni estado) debe mapear nulls y POR_ESTUDIAR")
    void toEntity_conDtoMinimo_mapeaCamposNulosYPorEstudiar() {
        PostulacionDTO dto = new PostulacionDTO(
                null,
                null,
                null,
                null,
                1L,
                1L
        );

        Postulacion resultado = postulacionMapper.toEntity(dto, licitacion, proveedor);

        assertNull(resultado.getMontoPostulacion());
        assertNull(resultado.getPropuestaTecnica());
        assertEquals(EstadoPostulacion.POR_ESTUDIAR, resultado.getEstado());
        assertSame(licitacion, resultado.getLicitacion());
        assertSame(proveedor, resultado.getProveedor());
    }

    @Test
    @DisplayName("toDTO debe mapear todos los campos, incluyendo los ids extraídos de las relaciones")
    void toDTO_mapeaTodosLosCamposIncluyendoIdsDeRelaciones() {
        Postulacion postulacion = new Postulacion();
        postulacion.setId(10L);
        postulacion.setMontoPostulacion(2000000.0);
        postulacion.setPropuestaTecnica("Propuesta Técnica Final");
        postulacion.setEstado(EstadoPostulacion.GANADA);
        postulacion.setLicitacion(licitacion);
        postulacion.setProveedor(proveedor);

        PostulacionDTO resultado = postulacionMapper.toDTO(postulacion);

        assertEquals(postulacion.getId(), resultado.id());
        assertEquals(postulacion.getMontoPostulacion(), resultado.montoPostulacion());
        assertEquals(postulacion.getPropuestaTecnica(), resultado.propuestaTecnica());
        assertEquals(postulacion.getEstado(), resultado.estado());
        assertEquals(licitacion.getId(), resultado.licitacionId());
        assertEquals(proveedor.getId(), resultado.proveedorId());
    }
}

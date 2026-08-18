package com.mercadopublico.mvp.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mercadopublico.mvp.dto.UsuarioDTO;
import com.mercadopublico.mvp.dto.UsuarioResponseDTO;
import com.mercadopublico.mvp.model.Usuario;

class UsuarioMapperTest {

    private final UsuarioMapper usuarioMapper = new UsuarioMapper();

    private UsuarioDTO usuarioDTO;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarioDTO = new UsuarioDTO("76543210-K", "Empresa Ejemplo SpA", "contacto@empresa.cl", "PROVEEDOR");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setRunOId("76543210-K");
        usuario.setNombre("Empresa Ejemplo SpA");
        usuario.setEmail("contacto@empresa.cl");
        usuario.setRol("PROVEEDOR");
        usuario.setFechaCreacion(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    @DisplayName("toEntity debe mapear todos los campos del DTO a la entidad")
    void toEntity_mapeaTodosLosCampos() {
        Usuario resultado = usuarioMapper.toEntity(usuarioDTO);

        assertEquals(usuarioDTO.runOId(), resultado.getRunOId());
        assertEquals(usuarioDTO.nombre(), resultado.getNombre());
        assertEquals(usuarioDTO.email(), resultado.getEmail());
        assertEquals(usuarioDTO.rol(), resultado.getRol());
    }

    @Test
    @DisplayName("toEntity no debe asignar id ni fechaCreacion, ya que son responsabilidad de la persistencia")
    void toEntity_noAsignaCamposDePersistencia() {
        Usuario resultado = usuarioMapper.toEntity(usuarioDTO);

        assertEquals(null, resultado.getId());
        assertEquals(null, resultado.getFechaCreacion());
    }

    @Test
    @DisplayName("toResponseDTO debe mapear todos los campos de la entidad, incluida fechaCreacion")
    void toResponseDTO_mapeaTodosLosCampos() {
        UsuarioResponseDTO resultado = usuarioMapper.toResponseDTO(usuario);

        assertEquals(usuario.getId(), resultado.id());
        assertEquals(usuario.getRunOId(), resultado.runOId());
        assertEquals(usuario.getNombre(), resultado.nombre());
        assertEquals(usuario.getEmail(), resultado.email());
        assertEquals(usuario.getRol(), resultado.rol());
        assertEquals(usuario.getFechaCreacion(), resultado.fechaCreacion());
    }

    @Test
    @DisplayName("toResponseDTOList debe mapear cada elemento de la lista preservando el orden")
    void toResponseDTOList_mapeaListaCompleta() {
        Usuario segundoUsuario = new Usuario();
        segundoUsuario.setId(2L);
        segundoUsuario.setRunOId("11222333-4");
        segundoUsuario.setNombre("Juan Pérez");
        segundoUsuario.setEmail("juan@correo.cl");
        segundoUsuario.setRol("COMPRADOR");
        segundoUsuario.setFechaCreacion(Instant.parse("2026-02-01T00:00:00Z"));

        List<UsuarioResponseDTO> resultado = usuarioMapper.toResponseDTOList(List.of(usuario, segundoUsuario));

        assertEquals(2, resultado.size());
        assertEquals(usuario.getId(), resultado.get(0).id());
        assertEquals(segundoUsuario.getId(), resultado.get(1).id());
        assertEquals(segundoUsuario.getNombre(), resultado.get(1).nombre());
    }

    @Test
    @DisplayName("toResponseDTOList debe retornar una lista vacía cuando recibe una lista vacía")
    void toResponseDTOList_listaVaciaRetornaListaVacia() {
        List<UsuarioResponseDTO> resultado = usuarioMapper.toResponseDTOList(List.of());

        assertTrue(resultado.isEmpty());
    }
}

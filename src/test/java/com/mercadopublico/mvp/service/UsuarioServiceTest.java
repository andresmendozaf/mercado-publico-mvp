package com.mercadopublico.mvp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mercadopublico.mvp.dto.UsuarioDTO;
import com.mercadopublico.mvp.dto.UsuarioResponseDTO;
import com.mercadopublico.mvp.exception.RecursoDuplicadoException;
import com.mercadopublico.mvp.exception.RecursoNoEncontradoException;
import com.mercadopublico.mvp.mapper.UsuarioMapper;
import com.mercadopublico.mvp.model.Usuario;
import com.mercadopublico.mvp.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioPrueba;
    private UsuarioDTO usuarioDtoPrueba;
    private UsuarioResponseDTO usuarioResponseEsperada;

    @BeforeEach
    void setUp() {
        // Datos de prueba que se reiniciarán antes de cada test
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setRunOId("76543210-K");
        usuarioPrueba.setNombre("Empresa Ejemplo SpA");
        usuarioPrueba.setEmail("contacto@empresa.cl");
        usuarioPrueba.setRol("PROVEEDOR");

        usuarioDtoPrueba = new UsuarioDTO("76543210-K", "Empresa Ejemplo SpA", "contacto@empresa.cl", "PROVEEDOR");

        usuarioResponseEsperada = new UsuarioResponseDTO(
                usuarioPrueba.getId(),
                usuarioPrueba.getRunOId(),
                usuarioPrueba.getNombre(),
                usuarioPrueba.getEmail(),
                usuarioPrueba.getRol(),
                usuarioPrueba.getFechaCreacion()
        );
    }

    @Test
    @DisplayName("Debe registrar un usuario exitosamente cuando no hay duplicados")
    void registrarUsuario_Exito() {
        // GIVEN (Dado que)
        when(usuarioMapper.toEntity(usuarioDtoPrueba)).thenReturn(usuarioPrueba);
        when(usuarioRepository.findByRunOId(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioPrueba);
        when(usuarioMapper.toResponseDTO(usuarioPrueba)).thenReturn(usuarioResponseEsperada);

        // WHEN (Cuando)
        UsuarioResponseDTO resultado = usuarioService.registrarUsuario(usuarioDtoPrueba);

        // THEN (Entonces)
        assertNotNull(resultado);
        assertEquals("76543210-K", resultado.runOId());
        verify(usuarioRepository, times(1)).save(usuarioPrueba); // Verificamos que se llamó a guardar
    }

    @Test
    @DisplayName("Debe lanzar RecursoDuplicadoException si el RUN ya existe")
    void registrarUsuario_FallaRunDuplicado() {
        // GIVEN
        when(usuarioMapper.toEntity(usuarioDtoPrueba)).thenReturn(usuarioPrueba);
        when(usuarioRepository.findByRunOId(usuarioPrueba.getRunOId()))
                .thenReturn(Optional.of(usuarioPrueba));

        // WHEN & THEN
        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> usuarioService.registrarUsuario(usuarioDtoPrueba)
        );

        assertEquals("El RUN/ID fiscal ya está registrado.", excepcion.getMessage());
        // Verificamos que NUNCA llegó a ejecutar el guardado
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe lanzar RecursoDuplicadoException si el Email ya existe")
    void registrarUsuario_FallaEmailDuplicado() {
        // GIVEN
        when(usuarioMapper.toEntity(usuarioDtoPrueba)).thenReturn(usuarioPrueba);
        when(usuarioRepository.findByRunOId(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(usuarioPrueba.getEmail()))
                .thenReturn(Optional.of(usuarioPrueba));

        // WHEN & THEN
        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> usuarioService.registrarUsuario(usuarioDtoPrueba)
        );

        assertEquals("El correo electrónico ya está registrado.", excepcion.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe retornar la lista completa de usuarios")
    void obtenerTodos_Exito() {
        // GIVEN
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioPrueba));
        when(usuarioMapper.toResponseDTOList(List.of(usuarioPrueba))).thenReturn(List.of(usuarioResponseEsperada));

        // WHEN
        List<UsuarioResponseDTO> resultados = usuarioService.obtenerTodos();

        // THEN
        assertFalse(resultados.isEmpty());
        assertEquals(1, resultados.size());
        assertEquals("Empresa Ejemplo SpA", resultados.get(0).nombre());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar un usuario cuando el ID existe")
    void obtenerPorId_Exito() {
        // GIVEN
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));
        when(usuarioMapper.toResponseDTO(usuarioPrueba)).thenReturn(usuarioResponseEsperada);

        // WHEN
        UsuarioResponseDTO resultado = usuarioService.obtenerPorId(1L);

        // THEN
        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("contacto@empresa.cl", resultado.email());
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException cuando el ID no existe")
    void obtenerPorId_FallaNoEncontrado() {
        // GIVEN
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(
                RecursoNoEncontradoException.class, 
                () -> usuarioService.obtenerPorId(99L)
        );

        assertEquals("Usuario no encontrado con el ID: 99", excepcion.getMessage());
    }
}
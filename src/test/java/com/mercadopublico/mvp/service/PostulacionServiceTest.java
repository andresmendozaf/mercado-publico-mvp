package com.mercadopublico.mvp.service;

import com.mercadopublico.mvp.dto.PostulacionDTO;
import com.mercadopublico.mvp.exception.RecursoNoEncontradoException;
import com.mercadopublico.mvp.mapper.PostulacionMapper;
import com.mercadopublico.mvp.model.EstadoPostulacion;
import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.model.Postulacion;
import com.mercadopublico.mvp.model.Usuario;
import com.mercadopublico.mvp.repository.LicitacionRepository;
import com.mercadopublico.mvp.repository.PostulacionRepository;
import com.mercadopublico.mvp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostulacionServiceTest {

    @Mock
    private PostulacionRepository postulacionRepository;

    @Mock
    private LicitacionRepository licitacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PostulacionMapper postulacionMapper;

    @InjectMocks
    private PostulacionService postulacionService;

    private Licitacion licitacionPrueba;
    private Usuario proveedorPrueba;
    private Postulacion postulacionPrueba;
    private PostulacionDTO postulacionDTOPrueba;

    @BeforeEach
    void setUp() {
        licitacionPrueba = new Licitacion();
        licitacionPrueba.setId(1L);

        proveedorPrueba = new Usuario();
        proveedorPrueba.setId(1L);

        postulacionPrueba = new Postulacion();
        postulacionPrueba.setId(10L);
        postulacionPrueba.setMontoPostulacion(1500000.0);
        postulacionPrueba.setPropuestaTecnica("Propuesta de Desarrollo");
        postulacionPrueba.setEstado(EstadoPostulacion.POR_ESTUDIAR);
        postulacionPrueba.setLicitacion(licitacionPrueba);
        postulacionPrueba.setProveedor(proveedorPrueba);

        postulacionDTOPrueba = new PostulacionDTO(
                10L,
                1500000.0,
                "Propuesta de Desarrollo",
                EstadoPostulacion.POR_ESTUDIAR,
                1L,
                1L
        );
    }

    @Nested
    @DisplayName("Pruebas de Creación (crearPostulacion)")
    class CrearPostulacionTests {

        @Test
        @DisplayName("Debe crear una postulación exitosamente cuando la licitación y el usuario existen")
        void debeCrearPostulacionExitosamente() {
            // Arrange
            when(licitacionRepository.findById(1L)).thenReturn(Optional.of(licitacionPrueba));
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(proveedorPrueba));
            when(postulacionMapper.toEntity(postulacionDTOPrueba, licitacionPrueba, proveedorPrueba))
                    .thenReturn(postulacionPrueba);
            when(postulacionRepository.save(any(Postulacion.class))).thenReturn(postulacionPrueba);
            when(postulacionMapper.toDTO(postulacionPrueba)).thenReturn(postulacionDTOPrueba);

            // Act
            PostulacionDTO resultado = postulacionService.crearPostulacion(postulacionDTOPrueba);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(10L);
            assertThat(resultado.montoPostulacion()).isEqualTo(1500000.0);
            verify(postulacionRepository, times(1)).save(any(Postulacion.class));
        }

        @Test
        @DisplayName("Debe lanzar RecursoNoEncontradoException cuando la licitación no existe")
        void debeLanzarExcepcionSiLicitacionNoExiste() {
            // Arrange
            when(licitacionRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> postulacionService.crearPostulacion(postulacionDTOPrueba))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("Licitación no encontrada: 1");

            verify(postulacionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar RecursoNoEncontradoException cuando el usuario proveedor no existe")
        void debeLanzarExcepcionSiUsuarioNoExiste() {
            // Arrange
            when(licitacionRepository.findById(1L)).thenReturn(Optional.of(licitacionPrueba));
            when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> postulacionService.crearPostulacion(postulacionDTOPrueba))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("Usuario no encontrado: 1");

            verify(postulacionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Pruebas de Consulta (obtenerPorProveedor)")
    class ConsultaTests {

        @Test
        @DisplayName("Debe obtener las postulaciones de un proveedor específico")
        void debeObtenerPorProveedor() {
            // Arrange
            when(postulacionRepository.findByProveedorId(1L)).thenReturn(List.of(postulacionPrueba));
            when(postulacionMapper.toDTO(postulacionPrueba)).thenReturn(postulacionDTOPrueba);

            // Act
            List<PostulacionDTO> lista = postulacionService.obtenerPorProveedor(1L);

            // Assert
            assertThat(lista).hasSize(1);
            assertThat(lista.get(0).proveedorId()).isEqualTo(1L);
            verify(postulacionRepository, times(1)).findByProveedorId(1L);
        }
    }

    @Nested
    @DisplayName("Pruebas de Cambio de Estado y Eliminación")
    class ModificacionTests {

        @Test
        @DisplayName("Debe cambiar el estado de la postulación exitosamente")
        void debeCambiarEstadoExitosamente() {
            // Arrange
            when(postulacionRepository.findById(10L)).thenReturn(Optional.of(postulacionPrueba));

            PostulacionDTO dtoActualizado = new PostulacionDTO(
                    10L, 1500000.0, "Propuesta", EstadoPostulacion.POSTULADA, 1L, 1L);
            when(postulacionMapper.toDTO(postulacionPrueba)).thenReturn(dtoActualizado);

            // Act
            PostulacionDTO resultado = postulacionService.cambiarEstado(10L, EstadoPostulacion.POSTULADA);

            // Assert
            assertThat(resultado.estado()).isEqualTo(EstadoPostulacion.POSTULADA);
            assertThat(postulacionPrueba.getEstado()).isEqualTo(EstadoPostulacion.POSTULADA);
        }

        @Test
        @DisplayName("Debe eliminar una postulación por ID")
        void debeEliminarPostulacion() {
            // Arrange
            when(postulacionRepository.existsById(10L)).thenReturn(true);

            // Act
            postulacionService.eliminarPostulacion(10L);

            // Assert
            verify(postulacionRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Debe lanzar excepción al intentar eliminar una postulación inexistente")
        void debeLanzarExcepcionAlEliminarInexistente() {
            // Arrange
            when(postulacionRepository.existsById(99L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> postulacionService.eliminarPostulacion(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("Postulación no encontrada: 99");

            verify(postulacionRepository, never()).deleteById(any());
        }
    }
    @Test
    @DisplayName("Debe obtener lista de postulaciones por proveedor y estado")
    void obtenerPorProveedorYEstado_Exito() {
        // ARRANGE: 1. Creamos las instancias que necesitamos para este test
        Postulacion postulacion = new Postulacion();
        postulacion.setId(10L);
        postulacion.setEstado(EstadoPostulacion.POR_ESTUDIAR);

        // Suponiendo que tu PostulacionDTO es un record, lo instanciamos con sus valores
        // Ajusta los campos según la estructura exacta de tu PostulacionDTO
        PostulacionDTO postulacionDTO = new PostulacionDTO(
                10L, 
                500000.0, 
                "Licitación de Prueba", 
                EstadoPostulacion.PERDIDA, 
                100L,
                1L
        );

        // 2. Le decimos al Mock del repositorio qué devolver
        when(postulacionRepository.findByProveedorIdAndEstado(1L, EstadoPostulacion.PERDIDA))
                .thenReturn(List.of(postulacion));
                
        // 3. Le decimos al Mock del mapper qué devolver cuando reciba la entidad
        when(postulacionMapper.toDTO(any(Postulacion.class)))
                .thenReturn(postulacionDTO);

        List<PostulacionDTO> resultado = postulacionService.obtenerPorProveedorYEstado(1L, EstadoPostulacion.PERDIDA);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).id()); // Usa .id() si es un Record, o .getId() si es clase
        
        // Verificamos que las dependencias fueron llamadas
        verify(postulacionRepository, times(1)).findByProveedorIdAndEstado(1L, EstadoPostulacion.PERDIDA);
        verify(postulacionMapper, times(1)).toDTO(any(Postulacion.class));
    }
    
    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException al intentar cambiar estado de una postulación inexistente")
    void cambiarEstado_PostulacionNoEncontrada_LanzaExcepcion() {
        // ARRANGE: Simulamos que el ID buscado NO existe en la base de datos (Optional.empty)
        Long postulacionIdInexistente = 99L;
        when(postulacionRepository.findById(postulacionIdInexistente))
                .thenReturn(Optional.empty());

        // ACT & ASSERT: Verificamos que lance la excepción exacta
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class, () -> {
            postulacionService.cambiarEstado(postulacionIdInexistente, EstadoPostulacion.PERDIDA);
        });

        // ASSERT ADICIONAL: Verificamos que el mensaje de error sea exactamente el esperado
        assertEquals("Postulación no encontrada: " + postulacionIdInexistente, excepcion.getMessage());

        // Verificamos que NUNCA se haya intentado guardar algo en la BD tras el fallo
        verify(postulacionRepository, never()).save(any());
        verify(postulacionMapper, never()).toDTO(any());
    }
}
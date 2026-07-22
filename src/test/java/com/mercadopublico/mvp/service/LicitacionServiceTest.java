package com.mercadopublico.mvp.service;

import com.mercadopublico.mvp.dto.LicitacionesResponseDTO;
import com.mercadopublico.mvp.dto.LicitacionesResponseDTO.LicitacionApiDTO;
import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.repository.LicitacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LicitacionServiceTest {

    @Mock
    private LicitacionRepository licitacionRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LicitacionService licitacionService;

    private Licitacion licitacionEjemplo;

    @BeforeEach
    void setUp() {
        // Inyectamos el valor simulado de la propiedad @Value("${mercadopublico.api.ticket}")
        ReflectionTestUtils.setField(licitacionService, "apiTicket", "ticket-de-prueba");

        // Preparamos un objeto de prueba de dominio
        licitacionEjemplo = new Licitacion();
        licitacionEjemplo.setId(1L);
        licitacionEjemplo.setCodigoExterno("1234-56-78");
        licitacionEjemplo.setNombre("Adquisición de Servidores de Prueba");
        licitacionEjemplo.setEstado(EstadoLicitacion.PUBLICADA);
    }

    @Test
    @DisplayName("Debe sincronizar e ingestar licitaciones del día exitosamente desde la API de Mercado Público")
    void sincronizarLicitacionesDelDia_Exito() {
        // ARRANGE: Mock de respuesta de la API externa
        LicitacionApiDTO apiDto = new LicitacionApiDTO(
                "1234-56-78",
                "Adquisición de Servidores de Prueba",
                5, // Código estado API equivalente a PUBLICADA
                "2026-08-30T15:00:00",
                "Descripción de prueba"
        );

        // Instanciación con los 3 parámetros requeridos por el record LicitacionesResponseDTO
        LicitacionesResponseDTO responseDTO = new LicitacionesResponseDTO(
                1,
                "2026-08-30",
                List.of(apiDto)
        );

        when(restTemplate.getForObject(anyString(), eq(LicitacionesResponseDTO.class)))
                .thenReturn(responseDTO);

        when(licitacionRepository.findByCodigoExterno("1234-56-78"))
                .thenReturn(Optional.empty());

        when(licitacionRepository.save(any(Licitacion.class)))
                .thenReturn(licitacionEjemplo);

        // ACT: Invocación del proceso masivo
        List<Licitacion> resultado = licitacionService.sincronizarLicitacionesDelDia();

        // ASSERT: Validaciones de integración limpia
        assertNotNull(resultado, "La lista retornada no debe ser nula");
        assertEquals(1, resultado.size());
        assertEquals("1234-56-78", resultado.get(0).getCodigoExterno());

        verify(restTemplate, times(1)).getForObject(anyString(), eq(LicitacionesResponseDTO.class));
        verify(licitacionRepository, times(1)).findByCodigoExterno("1234-56-78");
        verify(licitacionRepository, times(1)).save(any(Licitacion.class));
    }

    @Test
    @DisplayName("Debe retornar una lista vacía si la API externa responde nulo")
    void sincronizarLicitacionesDelDia_RespuestaNula() {
        // ARRANGE
        when(restTemplate.getForObject(anyString(), eq(LicitacionesResponseDTO.class)))
                .thenReturn(null);

        // ACT
        List<Licitacion> resultado = licitacionService.sincronizarLicitacionesDelDia();

        // ASSERT
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty(), "La lista devuelta debe estar vacía");
        verify(licitacionRepository, never()).save(any(Licitacion.class));
    }

    @Test
    @DisplayName("Debe obtener todas las licitaciones guardadas en la base de datos")
    void obtenerTodas_Exito() {
        // ARRANGE
        when(licitacionRepository.findAll())
                .thenReturn(List.of(licitacionEjemplo));

        // ACT
        List<Licitacion> resultado = licitacionService.obtenerTodas();

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("1234-56-78", resultado.get(0).getCodigoExterno());
        verify(licitacionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener únicamente las licitaciones que están en estado PUBLICADA (abiertas)")
    void obtenerLicitacionesAbiertas_Exito() {
        // ARRANGE
        when(licitacionRepository.findByEstado(EstadoLicitacion.PUBLICADA))
                .thenReturn(List.of(licitacionEjemplo));

        // ACT
        List<Licitacion> resultado = licitacionService.obtenerLicitacionesAbiertas();

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(EstadoLicitacion.PUBLICADA, resultado.get(0).getEstado());
        verify(licitacionRepository, times(1)).findByEstado(EstadoLicitacion.PUBLICADA);
    }
}
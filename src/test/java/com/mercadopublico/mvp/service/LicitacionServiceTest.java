package com.mercadopublico.mvp.service;

import com.mercadopublico.mvp.client.ChileCompraClient;
import com.mercadopublico.mvp.dto.LicitacionResponseDTO;
import com.mercadopublico.mvp.dto.MercadoPublicoLicitacionDTO;
import com.mercadopublico.mvp.exception.RecursoNoEncontradoException;
import com.mercadopublico.mvp.mapper.LicitacionMapper;
import com.mercadopublico.mvp.mapper.LicitacionSyncMapper;
import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.model.LicitacionCambioCampo;
import com.mercadopublico.mvp.model.LicitacionSincronizacionLog;
import com.mercadopublico.mvp.repository.LicitacionRepository;
import com.mercadopublico.mvp.repository.LicitacionSincronizacionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LicitacionServiceTest {

        @Mock
        private LicitacionRepository licitacionRepository;

        @Mock
        private ChileCompraClient chileCompraClient;

        @Mock
        private LicitacionMapper licitacionMapper; // <-- Mock del componente Mapper extraído

        @Mock
        private LicitacionSyncMapper licitacionSyncMapper; // <-- Mock de la frontera de integración externa

        @Mock
        private LicitacionSincronizacionLogRepository licitacionSincronizacionLogRepository;

        @InjectMocks
        private LicitacionService licitacionService;

        private Licitacion licitacionEjemplo;
        private LicitacionResponseDTO responseEjemplo;
        private MercadoPublicoLicitacionDTO dtoEjemplo;

        @BeforeEach
        void setUp() {
                licitacionEjemplo = new Licitacion();
                licitacionEjemplo.setId(1L);
                licitacionEjemplo.setCodigoExterno("1234-56-78");
                licitacionEjemplo.setNombre("Adquisición de Servidores de Prueba");
                licitacionEjemplo.setEstado(EstadoLicitacion.PUBLICADA);

                responseEjemplo = new LicitacionResponseDTO(
                                1L,
                                "1234-56-78",
                                "Adquisición de Servidores de Prueba",
                                null,
                                null,
                                EstadoLicitacion.PUBLICADA,
                                null,
                                null,
                                null,
                                null,
                                null);

                // Constructor del DTO con los 8 parámetros actualizados
                dtoEjemplo = new MercadoPublicoLicitacionDTO(
                                "1234-56-78",
                                "Adquisición de Servidores de Prueba",
                                5,
                                "2026-08-30T15:00:00",
                                "Descripción detallada del requerimiento",
                                2500000.0,
                                "Ministerio de Defensa",
                                "98.765.432-1");
        }

        @Test
        @DisplayName("Debe sincronizar y ACTUALIZAR licitaciones si ya existen en la base de datos")
        void sincronizarLicitacionesDelDia_ActualizacionExitosa() {

                // GIVEN
                Licitacion existente = new Licitacion();
                existente.setId(1L);
                existente.setCodigoExterno("1234-56-78");
                existente.setNombre("Nombre anterior");
                existente.setEstado(EstadoLicitacion.PUBLICADA);

                when(chileCompraClient.obtenerLicitacionesPorFecha(anyString()))
                                .thenReturn(List.of(dtoEjemplo));

                when(licitacionRepository.findAllByCodigoExternoIn(anyList()))
                                .thenReturn(List.of(existente));

                /*
                 * Simulamos el comportamiento del mapper:
                 * modifica la misma entidad existente.
                 */
                doAnswer(invocation -> {
                        Licitacion licitacion = invocation.getArgument(1);

                        licitacion.setNombre(dtoEjemplo.nombre());

                        return null;
                }).when(licitacionSyncMapper)
                                .actualizarEntidadDesdeDto(eq(dtoEjemplo), same(existente));

                when(licitacionRepository.saveAll(anyList()))
                                .thenReturn(List.of(existente));

                when(licitacionMapper.toResponseDTOList(anyList()))
                                .thenReturn(List.of(responseEjemplo));

                // WHEN
                List<LicitacionResponseDTO> resultado = licitacionService.sincronizarLicitacionesDelDia();

                // THEN
                assertNotNull(resultado);
                assertEquals(1, resultado.size());

                verify(licitacionRepository)
                                .findAllByCodigoExternoIn(anyList());

                verify(licitacionSyncMapper)
                                .actualizarEntidadDesdeDto(eq(dtoEjemplo), same(existente));

                verify(licitacionRepository)
                                .saveAll(anyList());

                verify(licitacionSincronizacionLogRepository)
                                .saveAll(anyList());

                verify(licitacionMapper)
                                .toResponseDTOList(anyList());
        }

        @Test
        @DisplayName("Debe sincronizar y CREAR licitaciones si NO existen en la base de datos")
        void sincronizarLicitacionesDelDia_CreacionExitosa() {

                when(chileCompraClient.obtenerLicitacionesPorFecha(anyString()))
                                .thenReturn(List.of(dtoEjemplo));

                when(licitacionRepository.findAllByCodigoExternoIn(anyList()))
                                .thenReturn(List.of());

                when(licitacionRepository.saveAll(anyList()))
                                .thenReturn(List.of(licitacionEjemplo));

                when(licitacionSincronizacionLogRepository.saveAll(anyList()))
                                .thenReturn(List.of());

                when(licitacionMapper.toResponseDTOList(anyList()))
                                .thenReturn(List.of(responseEjemplo));

                List<LicitacionResponseDTO> resultado = licitacionService.sincronizarLicitacionesDelDia();

                assertNotNull(resultado);
                assertEquals(1, resultado.size());

                verify(chileCompraClient)
                                .obtenerLicitacionesPorFecha(anyString());

                verify(licitacionRepository)
                                .findAllByCodigoExternoIn(anyList());

                verify(licitacionSyncMapper)
                                .actualizarEntidadDesdeDto(
                                                eq(dtoEjemplo),
                                                any(Licitacion.class));

                verify(licitacionRepository)
                                .saveAll(anyList());

                verify(licitacionSincronizacionLogRepository)
                                .saveAll(anyList());

                verify(licitacionMapper)
                                .toResponseDTOList(anyList());
        }

        @Test
        @DisplayName("Debe tratar dos DTO con el mismo codigoExterno en el mismo listado como una sola licitación")
        void sincronizarLicitacionesDelDia_CodigoExternoRepetidoEnElMismoListado_NoCreaDuplicados() {

                // GIVEN: ChileCompra entrega dos DTO distintos para el mismo codigoExterno
                // (caso real detectado en producción: 621-420-LR26)
                String codigoDuplicado = "621-420-LR26";

                MercadoPublicoLicitacionDTO dtoPrimeraAparicion = new MercadoPublicoLicitacionDTO(
                                codigoDuplicado,
                                "Nombre versión 1",
                                5,
                                "2026-08-30T15:00:00",
                                "Descripción v1",
                                1000000.0,
                                "Ministerio de Defensa",
                                "98.765.432-1");

                MercadoPublicoLicitacionDTO dtoSegundaAparicion = new MercadoPublicoLicitacionDTO(
                                codigoDuplicado,
                                "Nombre versión 2 (última)",
                                5,
                                "2026-08-30T15:00:00.500",
                                "Descripción v2",
                                2000000.0,
                                "Ministerio de Defensa",
                                "98.765.432-1");

                when(chileCompraClient.obtenerLicitacionesPorFecha(anyString()))
                                .thenReturn(List.of(dtoPrimeraAparicion, dtoSegundaAparicion));

                // No existe nada en BD todavía para este código
                when(licitacionRepository.findAllByCodigoExternoIn(anyList()))
                                .thenReturn(List.of());

                // Simulamos el mapper real: sobrescribe codigoExterno y nombre según el DTO recibido
                doAnswer(invocation -> {
                        MercadoPublicoLicitacionDTO dtoArg = invocation.getArgument(0);
                        Licitacion licitacionArg = invocation.getArgument(1);
                        licitacionArg.setCodigoExterno(dtoArg.codigoExterno());
                        licitacionArg.setNombre(dtoArg.nombre());
                        licitacionArg.setEstado(EstadoLicitacion.PUBLICADA);
                        return null;
                }).when(licitacionSyncMapper)
                                .actualizarEntidadDesdeDto(any(MercadoPublicoLicitacionDTO.class), any(Licitacion.class));

                when(licitacionRepository.saveAll(anyList()))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                when(licitacionMapper.toResponseDTOList(anyList()))
                                .thenReturn(List.of(responseEjemplo));

                // WHEN
                List<LicitacionResponseDTO> resultado = licitacionService.sincronizarLicitacionesDelDia();

                // THEN: no debe lanzar excepción y debe persistir una única entidad para el código repetido
                assertNotNull(resultado);

                ArgumentCaptor<List<Licitacion>> entidadesCaptor = ArgumentCaptor.forClass(List.class);
                verify(licitacionRepository).saveAll(entidadesCaptor.capture());

                List<Licitacion> entidadesGuardadas = entidadesCaptor.getValue();

                long cantidadParaElCodigoDuplicado = entidadesGuardadas.stream()
                                .filter(l -> codigoDuplicado.equals(l.getCodigoExterno()))
                                .count();

                assertEquals(1, cantidadParaElCodigoDuplicado,
                                "Debe existir una única entidad para el codigoExterno repetido en el mismo lote");

                // La entidad final debe reflejar la última información procesada
                Licitacion licitacionFinal = entidadesGuardadas.stream()
                                .filter(l -> codigoDuplicado.equals(l.getCodigoExterno()))
                                .findFirst()
                                .orElseThrow();

                assertEquals("Nombre versión 2 (última)", licitacionFinal.getNombre());

                // El mapper debe haber sido invocado dos veces (una por cada aparición del DTO)
                verify(licitacionSyncMapper, times(2))
                                .actualizarEntidadDesdeDto(any(MercadoPublicoLicitacionDTO.class), any(Licitacion.class));
        }

        @Test
        @DisplayName("Debe retornar una lista vacía si la API externa responde estrictamente nulo")
        void sincronizarLicitacionesDelDia_RespuestaNula() {
                when(chileCompraClient.obtenerLicitacionesPorFecha(anyString()))
                                .thenReturn(null);

                List<LicitacionResponseDTO> resultado = licitacionService.sincronizarLicitacionesDelDia();

                assertNotNull(resultado);
                assertTrue(resultado.isEmpty());
                verify(licitacionRepository, never()).save(any());
                verify(licitacionSyncMapper, never()).actualizarEntidadDesdeDto(any(), any());
        }

        @Test
        @DisplayName("Debe retornar una lista vacía si la API externa responde con una lista vacía")
        void sincronizarLicitacionesDelDia_ListaVacia() {
                when(chileCompraClient.obtenerLicitacionesPorFecha(anyString()))
                                .thenReturn(Collections.emptyList());

                List<LicitacionResponseDTO> resultado = licitacionService.sincronizarLicitacionesDelDia();

                assertNotNull(resultado);
                assertTrue(resultado.isEmpty());
                verify(licitacionRepository, never()).save(any());
                verify(licitacionSyncMapper, never()).actualizarEntidadDesdeDto(any(), any());
        }

        @Test
        @DisplayName("Debe obtener todas las licitaciones guardadas en la base de datos")
        void obtenerTodas_Exito() {
                when(licitacionRepository.findAll()).thenReturn(List.of(licitacionEjemplo));
                when(licitacionMapper.toResponseDTOList(anyList())).thenReturn(List.of(responseEjemplo));
                List<LicitacionResponseDTO> resultado = licitacionService.obtenerTodas();
                assertEquals(1, resultado.size());
                verify(licitacionRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Debe obtener el detalle de una licitación existente por ID")
        void obtenerPorId_Exito() {
                when(licitacionRepository.findById(1L)).thenReturn(Optional.of(licitacionEjemplo));
                when(licitacionMapper.toResponseDTO(licitacionEjemplo)).thenReturn(responseEjemplo);

                LicitacionResponseDTO resultado = licitacionService.obtenerPorId(1L);

                assertEquals(responseEjemplo, resultado);
                verify(licitacionRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Debe lanzar RecursoNoEncontradoException si el ID no existe")
        void obtenerPorId_NoExiste_LanzaExcepcion() {
                when(licitacionRepository.findById(99L)).thenReturn(Optional.empty());

                assertThrows(RecursoNoEncontradoException.class,
                                () -> licitacionService.obtenerPorId(99L));

                verify(licitacionRepository, times(1)).findById(99L);
        }

        @Test
        @DisplayName("Debe obtener únicamente las licitaciones que están en estado PUBLICADA")
        void obtenerLicitacionesAbiertas_Exito() {
                when(licitacionRepository.findByEstado(EstadoLicitacion.PUBLICADA))
                                .thenReturn(List.of(licitacionEjemplo));
                when(licitacionMapper.toResponseDTOList(anyList())).thenReturn(List.of(responseEjemplo));
                List<LicitacionResponseDTO> resultado = licitacionService.obtenerLicitacionesAbiertas();
                assertEquals(1, resultado.size());
                verify(licitacionRepository, times(1)).findByEstado(EstadoLicitacion.PUBLICADA);
        }

        @Test
        @DisplayName("No debe generar log de actualización si la licitación no presenta cambios")
        void sincronizarLicitacionesDelDia_SinCambios_NoGeneraLogActualizacion() {

                Licitacion existente = new Licitacion();
                existente.setId(1L);
                existente.setCodigoExterno("1234-56-78");
                existente.setNombre("Adquisición de Servidores de Prueba");
                existente.setEstado(EstadoLicitacion.PUBLICADA);

                when(chileCompraClient.obtenerLicitacionesPorFecha(anyString()))
                                .thenReturn(List.of(dtoEjemplo));

                when(licitacionRepository.findAllByCodigoExternoIn(anyList()))
                                .thenReturn(List.of(existente));

                // El mapper no modifica la entidad,
                // por lo que antes y después serán iguales.
                doNothing().when(licitacionSyncMapper)
                                .actualizarEntidadDesdeDto(eq(dtoEjemplo), same(existente));

                when(licitacionRepository.saveAll(anyList()))
                                .thenReturn(List.of(existente));

                when(licitacionMapper.toResponseDTOList(anyList()))
                                .thenReturn(List.of(responseEjemplo));

                List<LicitacionResponseDTO> resultado = licitacionService.sincronizarLicitacionesDelDia();

                assertNotNull(resultado);
                assertEquals(1, resultado.size());

                verify(licitacionSyncMapper)
                                .actualizarEntidadDesdeDto(eq(dtoEjemplo), same(existente));

                verify(licitacionRepository)
                                .saveAll(anyList());

                verify(licitacionMapper)
                                .toResponseDTOList(anyList());
        }

        @Test
        @DisplayName("Debe registrar cambio cuando un campo pasa de null a un valor")
        void sincronizarLicitacionesDelDia_CambioDeNullAValor() {

                Licitacion existente = new Licitacion();
                existente.setId(1L);
                existente.setCodigoExterno("1234-56-78");
                existente.setNombre(null);
                existente.setEstado(EstadoLicitacion.PUBLICADA);

                when(chileCompraClient.obtenerLicitacionesPorFecha(anyString()))
                                .thenReturn(List.of(dtoEjemplo));

                when(licitacionRepository.findAllByCodigoExternoIn(anyList()))
                                .thenReturn(List.of(existente));

                doAnswer(invocation -> {
                        Licitacion licitacion = invocation.getArgument(1);
                        licitacion.setNombre("Adquisición de Servidores de Prueba");
                        return null;
                }).when(licitacionSyncMapper)
                                .actualizarEntidadDesdeDto(eq(dtoEjemplo), same(existente));

                when(licitacionRepository.saveAll(anyList()))
                                .thenReturn(List.of(existente));

                when(licitacionMapper.toResponseDTOList(anyList()))
                                .thenReturn(List.of(responseEjemplo));

                licitacionService.sincronizarLicitacionesDelDia();

                verify(licitacionSincronizacionLogRepository)
                                .saveAll(argThat(logs -> {
                                        LicitacionSincronizacionLog log = logs.iterator().next();
                                        LicitacionCambioCampo cambio = log.getCambios().get(0);

                                        return cambio.getValorAnterior() == null
                                                        && "Adquisición de Servidores de Prueba"
                                                                        .equals(cambio.getValorNuevo());
                                }));
        }

        @Test
        @DisplayName("Debe registrar cambio cuando un campo pasa de valor a null")
        void sincronizarLicitacionesDelDia_CambioDeValorANull() {

                Licitacion existente = new Licitacion();
                existente.setId(1L);
                existente.setCodigoExterno("1234-56-78");
                existente.setNombre("Nombre anterior");
                existente.setEstado(EstadoLicitacion.PUBLICADA);

                when(chileCompraClient.obtenerLicitacionesPorFecha(anyString()))
                                .thenReturn(List.of(dtoEjemplo));

                when(licitacionRepository.findAllByCodigoExternoIn(anyList()))
                                .thenReturn(List.of(existente));

                doAnswer(invocation -> {
                        Licitacion licitacion = invocation.getArgument(1);
                        licitacion.setNombre(null);
                        return null;
                }).when(licitacionSyncMapper)
                                .actualizarEntidadDesdeDto(eq(dtoEjemplo), same(existente));

                when(licitacionRepository.saveAll(anyList()))
                                .thenReturn(List.of(existente));

                when(licitacionMapper.toResponseDTOList(anyList()))
                                .thenReturn(List.of(responseEjemplo));

                licitacionService.sincronizarLicitacionesDelDia();

                verify(licitacionSincronizacionLogRepository)
                                .saveAll(argThat(logs -> {
                                        LicitacionSincronizacionLog log = logs.iterator().next();
                                        LicitacionCambioCampo cambio = log.getCambios().get(0);

                                        return "Nombre anterior".equals(cambio.getValorAnterior())
                                                        && cambio.getValorNuevo() == null;
                                }));
        }
}
package com.mercadopublico.mvp.client;

import com.mercadopublico.mvp.config.MercadoPublicoProperties;
import com.mercadopublico.mvp.dto.MercadoPublicoLicitacionDTO;
import com.mercadopublico.mvp.exception.IntegracionApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class ChileCompraClientTest {

    private MockRestServiceServer mockServer;
    private ChileCompraClient chileCompraClient;

    @Mock
    private MercadoPublicoProperties properties;

    @BeforeEach
    void setUp() {
        // 1. Configuramos las properties falsas
        when(properties.ticket()).thenReturn("ticket-secreto-123");

        // 2. Creamos un RestClient real pero enlazado a nuestro servidor falso
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClientMockeado = builder.build();

        // 3. Instanciamos nuestro cliente con las dependencias listas
        chileCompraClient = new ChileCompraClient(restClientMockeado, properties);
    }

    @Test
    @DisplayName("Debe retornar la lista de licitaciones al consultar por fecha exitosamente")
    void obtenerLicitacionesPorFecha_Exito() {
        // ARRANGE: Simulamos un JSON real de Mercado Público
        String jsonResponse = """
            {
                "Cantidad": 1,
                "FechaCreacion": "2026-08-30",
                "Listado": [
                    {
                        "CodigoExterno": "1234-56-78",
                        "Nombre": "Servidores Prueba",
                        "CodigoEstado": 5,
                        "FechaCierre": "2026-09-01T15:00:00"
                    }
                ]
            }
            """;

        // Le decimos al servidor falso: "Cuando te llamen a esta URL exacta, responde con este JSON"
        mockServer.expect(requestTo("/licitaciones.json?fecha=06072026&ticket=ticket-secreto-123"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // ACT
        List<MercadoPublicoLicitacionDTO> resultado = chileCompraClient.obtenerLicitacionesPorFecha("06072026");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("1234-56-78", resultado.get(0).codigoExterno());
        
        // Verifica que la llamada HTTP efectivamente ocurrió
        mockServer.verify(); 
    }

    @Test
    @DisplayName("Debe lanzar IntegracionApiException cuando Mercado Público devuelva un error 500")
    void obtenerLicitacionesPorFecha_ErrorServidor() {
        // ARRANGE: Simulamos que Mercado Público se cayó (HTTP 500)
        mockServer.expect(requestTo("/licitaciones.json?fecha=06072026&ticket=ticket-secreto-123"))
                .andRespond(withServerError());

        // ACT & ASSERT: Verificamos que nuestro bloque catch capture y lance nuestra propia excepción
        assertThrows(IntegracionApiException.class, () -> {
            chileCompraClient.obtenerLicitacionesPorFecha("06072026");
        });

        mockServer.verify();
    }

    @Test
    @DisplayName("Debe retornar licitaciones al buscar por palabra clave exitosamente")
    void buscarNecesidadesReales_Exito() {
        // ARRANGE
        String jsonResponse = """
            [
                {
                    "CodigoExterno": "999-88-77",
                    "Nombre": "Desarrollo Software",
                    "CodigoEstado": 5,
                    "FechaCierre": "2026-09-01T15:00:00"
                }
            ]
            """; // Nota: buscarNecesidadesReales devuelve una lista directa en tu código original

        mockServer.expect(requestTo("/licitaciones.json?keyword=software&ticket=ticket-secreto-123"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // ACT
        List<MercadoPublicoLicitacionDTO> resultado = chileCompraClient.buscarNecesidadesReales("software");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("999-88-77", resultado.get(0).codigoExterno());
        
        mockServer.verify();
    }

    @Test
    @DisplayName("Debe lanzar IntegracionApiException al buscar por palabra clave si la API falla")
    void buscarNecesidadesReales_ErrorServidor() {
        // ARRANGE: Simulamos que la API externa se cae al buscar palabras clave (HTTP 500)
        mockServer.expect(requestTo("/licitaciones.json?keyword=software&ticket=ticket-secreto-123"))
                .andRespond(withServerError());

        assertThrows(IntegracionApiException.class, () -> {
            chileCompraClient.buscarNecesidadesReales("software");
        });

        // Verificamos que se haya intentado hacer la petición
        mockServer.verify();
    }

    @Test
    @DisplayName("Debe retornar lista vacía si la API responde con un JSON nulo")
    void obtenerLicitacionesPorFecha_RespuestaNula() {
        // ARRANGE: Simulamos que la API responde HTTP 200 OK, 
        // pero el cuerpo del JSON es "null" (esto hace que RestClient asigne null a la variable 'response')
        mockServer.expect(requestTo("/licitaciones.json?fecha=06072026&ticket=ticket-secreto-123"))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

        // ACT: Ejecutamos el método
        List<MercadoPublicoLicitacionDTO> resultado = chileCompraClient.obtenerLicitacionesPorFecha("06072026");

        // ASSERT: Verificamos que el operador ternario haya retornado List.of() (una lista vacía, no un null)
        assertNotNull(resultado, "Debe retornar una lista, no null");
        assertTrue(resultado.isEmpty(), "La lista debe estar completamente vacía");
        
        // Verificamos que la llamada ocurrió
        mockServer.verify();
    }
}
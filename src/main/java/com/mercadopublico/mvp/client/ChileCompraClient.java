package com.mercadopublico.mvp.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.mercadopublico.mvp.dto.MercadoPublicoLicitacionDTO;
import com.mercadopublico.mvp.dto.MercadoPublicoResponse;
import com.mercadopublico.mvp.exception.IntegracionApiException;

@Component
public class ChileCompraClient {
private final RestClient restClient;

        public ChileCompraClient() {
            // CAMBIAMOS DE v2 A v1:
            this.restClient = RestClient.builder()
                    .baseUrl("https://api.mercadopublico.cl/servicios/v1/publico")
                    .build();
        }

    public List<MercadoPublicoLicitacionDTO> buscarNecesidadesReales(String palabraClave, String ticketId) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/licitaciones.json")
                            .queryParam("keyword", palabraClave)
                            .queryParam("ticket", ticketId)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<MercadoPublicoLicitacionDTO>>() {});
        } catch (RestClientException e) {
            // Captura fallos específicos de red, timeouts o HTTP 4xx/5xx de la API externa
            throw new IntegracionApiException("Fallo en la comunicación con la API de Mercado Público", e);
        }
    }

    /**
     * Consulta las licitaciones reales de una fecha específica usando tu ticket oficial.
     * La API real exige el formato de fecha: DDMMAAAA (ej: 06072026)
     */
    public List<MercadoPublicoLicitacionDTO> obtenerLicitacionesPorFecha(String fecha, String ticketId) {
        try {
            // Estructura exacta de la API de ChileCompra: /licitaciones.json?fecha=DDMMAAAA&ticket=CRPT...
            MercadoPublicoResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/licitaciones.json")
                            .queryParam("fecha", fecha)
                            .queryParam("ticket", ticketId)
                            .build())
                    .retrieve()
                    .body(MercadoPublicoResponse.class);

            return response != null ? response.listado() : List.of();

        } catch (RestClientException e) {
            throw new IntegracionApiException("Error al conectar con el servidor real de Mercado Público", e);
        }
    }
}

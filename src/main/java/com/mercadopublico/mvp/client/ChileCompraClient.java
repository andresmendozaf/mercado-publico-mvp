package com.mercadopublico.mvp.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.mercadopublico.mvp.config.MercadoPublicoProperties;
import com.mercadopublico.mvp.dto.MercadoPublicoLicitacionDTO;
import com.mercadopublico.mvp.dto.MercadoPublicoResponse;
import com.mercadopublico.mvp.exception.IntegracionApiException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ChileCompraClient {

    private final RestClient restClient;
    private final MercadoPublicoProperties properties;

    // Inyectamos el RestClient y nuestras Properties
    public ChileCompraClient(
            @Qualifier("chileCompraRestClient") RestClient restClient,
            MercadoPublicoProperties properties) {

        this.restClient = restClient;
        this.properties = properties;
    }

    // Eliminamos el ticketId de los parámetros
    public List<MercadoPublicoLicitacionDTO> buscarNecesidadesReales(String palabraClave) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/licitaciones.json")
                            .queryParam("keyword", palabraClave)
                            .queryParam("ticket", properties.ticket()) // Usamos la property directamente
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<MercadoPublicoLicitacionDTO>>() {});
        } catch (RestClientException e) {
            log.error("Fallo al buscar licitaciones por palabra clave: {}", palabraClave, e);
            throw new IntegracionApiException("Fallo en la comunicación con la API de Mercado Público", e);
        }
    }

    /**
     * Consulta las licitaciones reales de una fecha específica usando el ticket oficial.
     * La API real exige el formato de fecha: DDMMAAAA (ej: 06072026)
     */
    // Eliminamos el ticketId de los parámetros
    public List<MercadoPublicoLicitacionDTO> obtenerLicitacionesPorFecha(String fecha) {
        try {
            MercadoPublicoResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/licitaciones.json")
                            .queryParam("fecha", fecha)
                            .queryParam("ticket", properties.ticket()) // Usamos la property directamente
                            .build())
                    .retrieve()
                    .body(MercadoPublicoResponse.class);

            return response != null ? response.listado() : List.of();

        } catch (RestClientException e) {
            log.error("Error al obtener licitaciones para la fecha: {}", fecha, e);
            throw new IntegracionApiException("Error al conectar con el servidor real de Mercado Público", e);
        }
    }
}
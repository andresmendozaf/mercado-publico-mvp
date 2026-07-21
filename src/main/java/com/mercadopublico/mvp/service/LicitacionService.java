package com.mercadopublico.mvp.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mercadopublico.mvp.dto.LicitacionesResponseDTO;
import com.mercadopublico.mvp.dto.LicitacionesResponseDTO.LicitacionApiDTO;
import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.repository.LicitacionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LicitacionService {

    private final LicitacionRepository licitacionRepository;
    private final RestTemplate restTemplate;

    // Inyecta el ticket desde tu application.properties
    @Value("${mercadopublico.api.ticket}")
    private String apiTicket;

    // --- MÉTODOS LOCALES ---

    public Licitacion guardarLicitacionDirecta(Licitacion licitacion) {
        if (licitacion.getEstado() == null) {
            licitacion.setEstado(EstadoLicitacion.PUBLICADA);
        }
        return licitacionRepository.save(licitacion);
    }

    public List<Licitacion> obtenerLicitacionesAbiertas() {
        return licitacionRepository.findByEstado(EstadoLicitacion.PUBLICADA);
    }

    public List<Licitacion> obtenerTodas() {
        return licitacionRepository.findAll();
    }

    // --- SINCRONIZACIÓN CON MERCADO PÚBLICO ---

    public List<Licitacion> sincronizarLicitacionesDelDia() {
        String fechaHoy = LocalDate.now(ZoneId.of("America/Santiago"))
                                .format(DateTimeFormatter.ofPattern("ddMMyyyy"));

        String url = String.format(
            "https://api.mercadopublico.cl/servicios/v1/publico/licitaciones.json?fecha=%s&ticket=%s",
            fechaHoy,
            apiTicket
        );

        LicitacionesResponseDTO response = restTemplate.getForObject(url, LicitacionesResponseDTO.class);
        List<Licitacion> guardadas = new ArrayList<>();

        if (response != null && response.listado() != null) {
            for (LicitacionApiDTO dto : response.listado()) {
                
                // 1. Identificación y Fallbacks
                String codigo = (dto.codigoExterno() != null) ? dto.codigoExterno() : "SIN-CODIGO";
                String nombre = (dto.nombre() != null) ? dto.nombre() : "Licitación sin nombre provisto";

                // 2. Buscar si ya existe en PostgreSQL por su código externo único
                Licitacion licitacion = licitacionRepository.findByCodigoExterno(codigo)
                                        .orElseGet(Licitacion::new); // Si existe la actualiza, si no, crea una nueva

                // 3. Mapeo/Actualización de campos
                licitacion.setCodigoExterno(codigo);
                licitacion.setNombre(nombre);
                licitacion.setTitulo("[" + codigo + "] " + nombre);
                licitacion.setEstado(EstadoLicitacion.desdeCodigoApi(dto.codigoEstado()));
                licitacion.setDescripcion(nombre);
                licitacion.setPresupuestoEstimado(0.0);
                licitacion.setFechaCierre(parsearFechaCierre(dto.fechaCierre()));

                // 4. Guardar (JPA detectará si es INSERT o UPDATE de forma automática)
                guardadas.add(licitacionRepository.save(licitacion));
            }
        }

        return guardadas;
    }

    /**
     * Intenta parsear la fecha enviada por la API. 
     * En caso de venir nula, vacía o con un formato no reconocido,
     * aplica un fallback automático asignando 30 días a partir de hoy.
     */
    private Instant parsearFechaCierre(String fechaStr) {
        if (fechaStr != null && !fechaStr.isBlank()) {
            try {
                return java.time.LocalDateTime.parse(fechaStr)
                        .toInstant(java.time.ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
                log.warn("No se pudo parsear la fecha de cierre '{}'. Asignando fecha por defecto (+30 días). Error: {}", 
                         fechaStr, e.getMessage());
            }
        }
        return Instant.now().plus(30, ChronoUnit.DAYS);
    }
}
package com.mercadopublico.mvp.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    @Value("${mercadopublico.api.ticket}")
    private String apiTicket;

    // --- MÉTODOS LOCALES ---

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

                String codigo = (dto.codigoExterno() != null) ? dto.codigoExterno() : "SIN-CODIGO";
                String nombre = (dto.nombre() != null) ? dto.nombre() : "Licitación sin nombre provisto";

                // Buscar si ya existe para actualizar (UPSERT), o instanciar una nueva
                Licitacion licitacion = licitacionRepository.findByCodigoExterno(codigo)
                                        .orElseGet(Licitacion::new);

                // Mapeo atómico de campos reales
                licitacion.setCodigoExterno(codigo);
                licitacion.setNombre(nombre); // <- El nombre oficial devuelto por Mercado Público
                
                // Si tu DTO no trae descripción en la lista del día, no es necesario setearlo (o se setea null)
                if (dto.descripcion() != null) {
                    licitacion.setDescripcion(dto.descripcion());
                }

                licitacion.setEstado(EstadoLicitacion.desdeCodigoApi(dto.codigoEstado()));
                licitacion.setFechaCierre(parsearFechaCierre(dto.fechaCierre()));

                guardadas.add(licitacionRepository.save(licitacion));
            }
        }

        return guardadas;
    }

    private Instant parsearFechaCierre(String fechaStr) {
        if (fechaStr != null && !fechaStr.isBlank()) {
            try {
                return java.time.LocalDateTime.parse(fechaStr)
                        .toInstant(java.time.ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
                log.warn("No se pudo parsear la fecha de cierre '{}'. Dejando campo como null. Error: {}", 
                        fechaStr, e.getMessage());
            }
        }
        return null; 
    }
}
package com.mercadopublico.mvp.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mercadopublico.mvp.client.ChileCompraClient;
import com.mercadopublico.mvp.dto.MercadoPublicoLicitacionDTO;
import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.repository.LicitacionRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SincronizacionService {

    private final ChileCompraClient chileCompraClient;
    private final LicitacionRepository licitacionRepository;

    public SincronizacionService(ChileCompraClient chileCompraClient, LicitacionRepository licitacionRepository) {
        this.chileCompraClient = chileCompraClient;
        this.licitacionRepository = licitacionRepository;
    }

    @Transactional
    public List<Licitacion> buscarYSincronizar(String fecha, String ticketId) {
        List<MercadoPublicoLicitacionDTO> externas = chileCompraClient.obtenerLicitacionesPorFecha(fecha, ticketId);
        List<Licitacion> sincronizadas = new ArrayList<>();

        for (MercadoPublicoLicitacionDTO ext : externas) {
            Licitacion local = new Licitacion();
            
            // 1. Mapeo de Identificación
            String codigo = (ext.codigoExterno() != null) ? ext.codigoExterno() : "SIN-CODIGO";
            String nombre = (ext.nombre() != null) ? ext.nombre() : "Licitación sin nombre provisto";
            
            local.setCodigoExterno(codigo);
            local.setNombre(nombre);
            local.setTitulo("[" + codigo + "] " + nombre);
            
            // 2. Mapeo de Negocio
            local.setDescripcion("Licitación importada mediante sincronización masiva diaria. Detalle pendiente.");
            local.setPresupuestoEstimado(0.0);

            // 3. Estado (delegado al Enum)
            local.setEstado(EstadoLicitacion.desdeCodigoApi(ext.codigoEstado()));
            
            // 4. Parseo de Fecha
            local.setFechaCierre(parsearFechaCierre(ext.fechaCierre()));
            
            sincronizadas.add(licitacionRepository.save(local));
        }

        return sincronizadas;
    }

    private Instant parsearFechaCierre(String fechaStr) {
        if (fechaStr != null && !fechaStr.isBlank()) {
            try {
                return java.time.LocalDateTime.parse(fechaStr).toInstant(java.time.ZoneOffset.UTC);
            } catch (Exception e) {
                log.warn("No se pudo parsear la fecha de cierre '{}'. Aplicando fecha por defecto (+30 días). Error: {}", fechaStr, e.getMessage());
            }
        }
        return Instant.now().plus(30, java.time.temporal.ChronoUnit.DAYS);
    }
}
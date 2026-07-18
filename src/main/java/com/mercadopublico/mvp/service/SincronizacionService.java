package com.mercadopublico.mvp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mercadopublico.mvp.client.ChileCompraClient;
import com.mercadopublico.mvp.dto.MercadoPublicoLicitacionDTO;
import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.repository.LicitacionRepository;

import jakarta.transaction.Transactional;

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
            
            // 1. Validar campos de identificación base
            String codigo = (ext.codigoExterno() != null) ? ext.codigoExterno() : "SIN-CODIGO";
            String nombreLicitacion = (ext.nombre() != null) ? ext.nombre() : "Licitación sin nombre provisto";
            local.setTitulo("[" + codigo + "] " + nombreLicitacion);
            
            // 2. Nuevos campos planos alineados a tu modelo de negocio
            local.setDescripcion("Licitación importada mediante sincronización masiva diaria. Detalle pendiente.");
            local.setPresupuestoEstimado(0.0);
            local.setOrganismoComprador(null); // Se llenarán en la fase 2 con el endpoint de detalle
            local.setRutComprador(null); 

            // 3. Traducir el Código de Estado numérico oficial
            String estadoTexto = "OTRO"; // Cambiamos el valor por defecto de ACTIVA a OTRO
            if (ext.codigoEstado() != null) {
                estadoTexto = switch (ext.codigoEstado()) {
                    case 5  -> "PUBLICADA";   // Vigente y disponible para competir
                    case 6  -> "CERRADA";     // Plazo vencido, en evaluación
                    case 7  -> "DESIERTA";     // Nadie postuló o ninguna oferta sirvió ¡Mina de oro para analizar!
                    case 8  -> "ADJUDICADA";  // Ya tiene un proveedor ganador
                    case 9  -> "REVOCADA";    // Cancelada de raíz por el organismo público
                    case 15 -> "ADJUDICADA_ART3"; // Adjudicada por trato directo
                    case 18 -> "SUSPENDIDA";  // Congelada temporalmente por el Estado
                    default -> "CODIGO_" + ext.codigoEstado(); // Si aparece uno nuevo, guarda su número real para investigarlo
                };
            }
            local.setEstado(estadoTexto);
            
            // 4. Parsear la fecha de cierre real
            if (ext.fechaCierre() != null && !ext.fechaCierre().isBlank()) {
                try {
                    local.setFechaCierre(java.time.LocalDateTime.parse(ext.fechaCierre()).toInstant(java.time.ZoneOffset.UTC));
                } catch (Exception e) {
                    local.setFechaCierre(java.time.Instant.now().plus(15, java.time.temporal.ChronoUnit.DAYS));
                }
            } else {
                local.setFechaCierre(java.time.Instant.now().plus(30, java.time.temporal.ChronoUnit.DAYS));
            }
            
            sincronizadas.add(licitacionRepository.save(local));
        }

        return sincronizadas;
    }
}

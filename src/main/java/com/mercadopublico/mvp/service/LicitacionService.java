package com.mercadopublico.mvp.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercadopublico.mvp.client.ChileCompraClient;
import com.mercadopublico.mvp.dto.LicitacionResponseDTO;
import com.mercadopublico.mvp.dto.MercadoPublicoLicitacionDTO;
import com.mercadopublico.mvp.mapper.LicitacionMapper;
import com.mercadopublico.mvp.mapper.LicitacionSyncMapper;
import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.model.LicitacionCambioCampo;
import com.mercadopublico.mvp.model.LicitacionSincronizacionLog;
import com.mercadopublico.mvp.model.TipoOperacionSync;
import com.mercadopublico.mvp.repository.LicitacionRepository;
import com.mercadopublico.mvp.repository.LicitacionSincronizacionLogRepository;
import com.mercadopublico.mvp.repository.LicitacionSpecifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class LicitacionService {

    private final LicitacionRepository licitacionRepository;
    private final ChileCompraClient chileCompraClient;
    private final LicitacionMapper licitacionMapper;
    private final LicitacionSyncMapper licitacionSyncMapper;
    private final LicitacionSincronizacionLogRepository licitacionSincronizacionLogRepository;

    public List<LicitacionResponseDTO> obtenerLicitacionesAbiertas() {
        return licitacionMapper.toResponseDTOList(
                licitacionRepository.findByEstado(EstadoLicitacion.PUBLICADA));
    }

    public List<LicitacionResponseDTO> obtenerTodas() {
        return licitacionMapper.toResponseDTOList(licitacionRepository.findAll());
    }

    public List<LicitacionResponseDTO> buscarConFiltros(String texto, EstadoLicitacion estado, String organismo) {
        Specification<Licitacion> specification = Specification
                .where(LicitacionSpecifications.conTexto(texto))
                .and(LicitacionSpecifications.conEstado(estado))
                .and(LicitacionSpecifications.conOrganismo(organismo));

        return licitacionMapper.toResponseDTOList(licitacionRepository.findAll(specification));
    }

    // ---------------- SINCRONIZACIÓN IDEMPOTENTE ----------------

@Transactional
public List<LicitacionResponseDTO> sincronizarLicitacionesDelDia() {

    String fecha = LocalDate.now(ZoneId.of("America/Santiago"))
            .format(DateTimeFormatter.ofPattern("ddMMyyyy"));

    List<MercadoPublicoLicitacionDTO> listado = chileCompraClient.obtenerLicitacionesPorFecha(fecha);

    if (listado == null || listado.isEmpty()) {
        return List.of();
    }

    List<String> codigos = listado.stream()
            .map(MercadoPublicoLicitacionDTO::codigoExterno)
            .toList();

    Map<String, Licitacion> existentesPorCodigo = licitacionRepository
            .findAllByCodigoExternoIn(codigos)
            .stream()
            .collect(Collectors.toMap(Licitacion::getCodigoExterno, Function.identity()));

    List<Licitacion> entidades = new ArrayList<>();
    List<LicitacionSincronizacionLog> logsAGuardar = new ArrayList<>();
    Set<String> codigosEnEsteLote = new HashSet<>();
    Instant ahora = Instant.now();

    for (MercadoPublicoLicitacionDTO dto : listado) {

        String codigo = dto.codigoExterno();
        Licitacion existente = existentesPorCodigo.get(codigo);
        boolean esNueva = existente == null;

        Licitacion licitacion = esNueva ? new Licitacion() : existente;

        Map<String, Object> antes = esNueva ? Map.of() : capturarCamposRelevantes(licitacion);

        licitacionSyncMapper.actualizarEntidadDesdeDto(dto, licitacion);

        if (esNueva) {
            // Registramos la entidad recién creada para que una segunda aparición
            // del mismo código en este mismo lote la reutilice en vez de crear otra.
            existentesPorCodigo.put(codigo, licitacion);
        }

        if (codigosEnEsteLote.add(codigo)) {
            entidades.add(licitacion);
        }

        if (esNueva) {
            LicitacionSincronizacionLog headerLog = new LicitacionSincronizacionLog();
            headerLog.setLicitacion(licitacion);
            headerLog.setCodigoExterno(codigo);
            headerLog.setTipoOperacion(TipoOperacionSync.CREADA);
            headerLog.setFechaSincronizacion(ahora);
            headerLog.setCantidadCamposCambiados(0);
            logsAGuardar.add(headerLog);
        } else {
            Map<String, Object> despues = capturarCamposRelevantes(licitacion);
            List<LicitacionCambioCampo> cambios = compararCampos(antes, despues);

            if (!cambios.isEmpty()) {
                LicitacionSincronizacionLog headerLog = new LicitacionSincronizacionLog();
                headerLog.setLicitacion(licitacion);
                headerLog.setCodigoExterno(codigo);
                headerLog.setTipoOperacion(TipoOperacionSync.ACTUALIZADA);
                headerLog.setFechaSincronizacion(ahora);
                headerLog.setCantidadCamposCambiados(cambios.size());

                cambios.forEach(c -> c.setSincronizacionLog(headerLog));
                headerLog.setCambios(cambios);

                logsAGuardar.add(headerLog);
            }
            // si no hubo cambios, no se persiste nada de auditoría
        }
    }

    List<Licitacion> guardadas = licitacionRepository.saveAll(entidades);
    licitacionSincronizacionLogRepository.saveAll(logsAGuardar); // cascada guarda también los LicitacionCambioCampo

    return licitacionMapper.toResponseDTOList(guardadas);
}

private Map<String, Object> capturarCamposRelevantes(Licitacion l) {
    Map<String, Object> valores = new LinkedHashMap<>();
    valores.put("nombre", l.getNombre());
    valores.put("descripcion", l.getDescripcion());
    valores.put("presupuestoEstimado", l.getPresupuestoEstimado());
    valores.put("estado", l.getEstado());
    valores.put("fechaCierre", l.getFechaCierre());
    valores.put("organismoComprador", l.getOrganismoComprador());
    valores.put("rutComprador", l.getRutComprador());
    return valores;
}

private List<LicitacionCambioCampo> compararCampos(Map<String, Object> antes, Map<String, Object> despues) {
    List<LicitacionCambioCampo> cambios = new ArrayList<>();

    for (Map.Entry<String, Object> entry : antes.entrySet()) {
        String campo = entry.getKey();
        Object valorAntes = entry.getValue();
        Object valorDespues = despues.get(campo);

        if (!Objects.equals(valorAntes, valorDespues)) {
            LicitacionCambioCampo cambio = new LicitacionCambioCampo();
            cambio.setNombreCampo(campo);
            cambio.setValorAnterior(valorAntes != null ? valorAntes.toString() : null);
            cambio.setValorNuevo(valorDespues != null ? valorDespues.toString() : null);
            cambios.add(cambio);
        }
    }

    return cambios;
}

}
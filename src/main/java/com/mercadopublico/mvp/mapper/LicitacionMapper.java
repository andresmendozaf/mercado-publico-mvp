package com.mercadopublico.mvp.mapper;

import com.mercadopublico.mvp.dto.LicitacionResponseDTO;
import com.mercadopublico.mvp.model.Licitacion;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Contrato de salida propio del API. No conoce ni depende de la
 * representación de la API de Mercado Público.
 */
@Component
public class LicitacionMapper {

    /**
     * Convierte una Entidad JPA al contrato de salida del API.
     */
    public LicitacionResponseDTO toResponseDTO(Licitacion licitacion) {
        return new LicitacionResponseDTO(
                licitacion.getId(),
                licitacion.getCodigoExterno(),
                licitacion.getNombre(),
                licitacion.getDescripcion(),
                licitacion.getPresupuestoEstimado(),
                licitacion.getEstado(),
                licitacion.getFechaCierre(),
                licitacion.getOrganismoComprador(),
                licitacion.getRutComprador(),
                licitacion.getFechaCreacion(),
                licitacion.getFechaActualizacion()
        );
    }

    public List<LicitacionResponseDTO> toResponseDTOList(List<Licitacion> licitaciones) {
        return licitaciones.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

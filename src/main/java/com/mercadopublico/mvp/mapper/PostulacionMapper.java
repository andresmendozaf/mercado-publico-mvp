package com.mercadopublico.mvp.mapper;

import org.springframework.stereotype.Component;

import com.mercadopublico.mvp.dto.PostulacionDTO;
import com.mercadopublico.mvp.model.EstadoPostulacion;
import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.model.Postulacion;
import com.mercadopublico.mvp.model.Usuario;

@Component
public class PostulacionMapper {

    /**
     * Convierte de DTO + Relaciones a Entidad JPA
     */
    public Postulacion toEntity(PostulacionDTO dto, Licitacion licitacion, Usuario proveedor) {
        Postulacion postulacion = new Postulacion();
        postulacion.setMontoPostulacion(dto.montoPostulacion());
        postulacion.setPropuestaTecnica(dto.propuestaTecnica());
        postulacion.setEstado(dto.estado() != null ? dto.estado() : EstadoPostulacion.POR_ESTUDIAR);
        postulacion.setLicitacion(licitacion);
        postulacion.setProveedor(proveedor);
        return postulacion;
    }

    /**
     * Convierte de Entidad JPA a DTO de salida
     */
    public PostulacionDTO toDTO(Postulacion postulacion) {
        return new PostulacionDTO(
            postulacion.getId(),
            postulacion.getMontoPostulacion(),
            postulacion.getPropuestaTecnica(),
            postulacion.getEstado(),
            postulacion.getLicitacion().getId(),
            postulacion.getProveedor().getId()
        );
    }
}
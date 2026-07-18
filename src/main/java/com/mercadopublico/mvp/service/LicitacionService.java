package com.mercadopublico.mvp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.repository.LicitacionRepository;

@Service
public class LicitacionService {
    
    private final LicitacionRepository licitacionRepository;

    public LicitacionService(LicitacionRepository licitacionRepository) {
        this.licitacionRepository = licitacionRepository;
    }

    public Licitacion guardarLicitacionDirecta(Licitacion licitacion) {
        if (licitacion.getEstado() == null || licitacion.getEstado().isBlank()) {
            licitacion.setEstado("PUBLICADA");
        }
        return licitacionRepository.save(licitacion);
    }

    public List<Licitacion> obtenerLicitacionesAbiertas() {
        // Retorna las disponibles para competir en tu negocio
        return licitacionRepository.findByEstado("PUBLICADA");
    }

    public List<Licitacion> obtenerTodas() {
        return licitacionRepository.findAll();
    }
}

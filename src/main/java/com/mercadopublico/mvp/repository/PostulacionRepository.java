package com.mercadopublico.mvp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadopublico.mvp.model.Postulacion;

public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
    // Permite al comprador ver todas las postulaciones de una licitación concreta
    List<Postulacion> findByLicitacionId(Long licitacionId);

}

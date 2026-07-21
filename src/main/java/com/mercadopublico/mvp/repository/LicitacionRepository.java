package com.mercadopublico.mvp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.Licitacion;

public interface LicitacionRepository extends JpaRepository<Licitacion, Long>{
 
    // Busca todas las licitaciones que tengan un estado en específico (ej: "ABIERTA")
    List<Licitacion> findByEstado(String estado);
    List<Licitacion> findByEstado(EstadoLicitacion estado);
    Optional<Licitacion> findByCodigoExterno(String codigoExterno);
}


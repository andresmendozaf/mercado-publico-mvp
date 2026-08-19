package com.mercadopublico.mvp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.Licitacion;

public interface LicitacionRepository extends JpaRepository<Licitacion, Long> {

    List<Licitacion> findByEstado(EstadoLicitacion estado);

    List<Licitacion> findAllByCodigoExternoIn(List<String> codigos);

}

package com.mercadopublico.mvp.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mercadopublico.mvp.model.LicitacionSincronizacionLog;

public interface LicitacionSincronizacionLogRepository extends JpaRepository<LicitacionSincronizacionLog, Long>{

    List<LicitacionSincronizacionLog> findByCodigoExternoOrderByFechaSincronizacionDesc(String codigoExterno);

    List<LicitacionSincronizacionLog> findByFechaSincronizacionBetween(Instant desde, Instant hasta);

    @Query("""
        SELECT l.tipoOperacion, COUNT(l)
        FROM LicitacionSincronizacionLog l
        WHERE l.fechaSincronizacion BETWEEN :desde AND :hasta
        GROUP BY l.tipoOperacion
        """)
    List<Object[]> resumenPorTipoOperacion(Instant desde, Instant hasta);
}

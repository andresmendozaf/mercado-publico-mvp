package com.mercadopublico.mvp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadopublico.mvp.model.EstadoPostulacion;
import com.mercadopublico.mvp.model.Postulacion;

public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
    
    // Obtener todas las postulaciones asociadas a una licitación
    List<Postulacion> findByLicitacionId(Long licitacionId);

    // Obtener el tablero/historial completo de postulaciones de un proveedor específico
    List<Postulacion> findByProveedorId(Long proveedorId);

    // Filtrar postulaciones de un proveedor por columna del Kanban (ej: POR_ESTUDIAR)
    List<Postulacion> findByProveedorIdAndEstado(Long proveedorId, EstadoPostulacion estado);
}
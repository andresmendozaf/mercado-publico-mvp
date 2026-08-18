package com.mercadopublico.mvp.model;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "licitaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Licitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigoExterno;

    @Column(columnDefinition = "TEXT")
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column
    private Double presupuestoEstimado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoLicitacion estado;

    @Column(name = "fecha_cierre")
    private Instant fechaCierre;

    @Column(name = "organismo_comprador")
    private String organismoComprador;

    @Column(name = "rut_comprador")
    private String rutComprador;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private Instant fechaActualizacion;

    @PrePersist
    public void prePersist() {
        Instant ahora = Instant.now();
        this.fechaCreacion = ahora;
        this.fechaActualizacion = ahora;
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = Instant.now();
    }

}
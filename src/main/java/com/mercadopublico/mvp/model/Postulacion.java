package com.mercadopublico.mvp.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "postulacion",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_postulacion_proveedor_licitacion", 
            columnNames = {"proveedor_id", "licitacion_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Postulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double montoPostulacion;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String propuestaTecnica;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPostulacion estado;

    @Column(nullable = false, updatable = false)
    private Instant fechaPostulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licitacion_id", nullable = false)
    private Licitacion licitacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Usuario proveedor;

    @PrePersist
    protected void onCreate() {
        this.fechaPostulacion = Instant.now();
        if (this.estado == null) {
            this.estado = EstadoPostulacion.POR_ESTUDIAR; // Estado inicial por defecto
        }
    }
}
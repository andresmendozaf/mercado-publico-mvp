package com.mercadopublico.mvp.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "postulacion")
@Data
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

    private Instant fechaPresentacion;

    @ManyToOne
    @JoinColumn(name = "licitacion_id", nullable = false)
    private Licitacion licitacion; // A qué licitación se postula

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Usuario proveedor; // Qué empresa/proveedor realiza la oferta

    @PrePersist
    protected void onCreate() {
        this.fechaPresentacion = Instant.now();
    }

}

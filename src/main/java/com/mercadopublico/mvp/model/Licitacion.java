package com.mercadopublico.mvp.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(columnDefinition = "TEXT")
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Double presupuestoEstimado;

    @Column(nullable = false)
    private String estado; 

    private Instant fechaCierre;

    @Column(name = "organismo_comprador", nullable = true)
    private String organismoComprador; 

    @Column(name = "rut_comprador", nullable = true)
    private String rutComprador;

}

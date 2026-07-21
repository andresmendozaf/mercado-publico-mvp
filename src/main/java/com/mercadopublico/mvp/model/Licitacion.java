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
    private String titulo; // Si deseas mantenerlo, o puedes calcularlo dinámicamente

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Double presupuestoEstimado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoLicitacion estado; 

    private Instant fechaCierre;

    @Column(name = "organismo_comprador")
    private String organismoComprador; 

    @Column(name = "rut_comprador")
    private String rutComprador;
}
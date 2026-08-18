package com.mercadopublico.mvp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "licitacion_cambio_campo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicitacionCambioCampo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sincronizacion_log_id", nullable = false)
    private LicitacionSincronizacionLog sincronizacionLog;

    @Column(name = "nombre_campo", nullable = false)
    private String nombreCampo;

    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    @Column(name = "valor_nuevo", columnDefinition = "TEXT")
    private String valorNuevo;
}
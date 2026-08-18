package com.mercadopublico.mvp.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "licitacion_sincronizacion_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicitacionSincronizacionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licitacion_id", nullable = false)
    private Licitacion licitacion;

    @Column(name = "codigo_externo", nullable = false)
    private String codigoExterno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoOperacionSync tipoOperacion;

    @Column(name = "cantidad_campos_cambiados")
    private Integer cantidadCamposCambiados;

    @Column(name = "fecha_sincronizacion", nullable = false)
    private Instant fechaSincronizacion;

    @OneToMany(mappedBy = "sincronizacionLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LicitacionCambioCampo> cambios = new ArrayList<>();
}
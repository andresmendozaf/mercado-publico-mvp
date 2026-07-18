package com.mercadopublico.mvp.model;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data // Genera Getters, Setters, toString, equals y hashCode automáticamente
@NoArgsConstructor // Genera el constructor vacío exigido por JPA
@AllArgsConstructor // Genera un constructor con todos los campos
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String runOId; // Identificador fiscal/RUT/RUN del usuario o empresa

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String rol; // Ejemplo: "COMPRADOR" o "PROVEEDOR"

    private Instant fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = Instant.now();
    }
}

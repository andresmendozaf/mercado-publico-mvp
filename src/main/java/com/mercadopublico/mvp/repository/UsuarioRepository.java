package com.mercadopublico.mvp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadopublico.mvp.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{

    // Método personalizado para buscar por RUT/RUN/ID Fiscal
    Optional<Usuario> findByRunOId(String runOId);
    
    // Método personalizado para buscar por Email
    Optional<Usuario> findByEmail(String email);
}
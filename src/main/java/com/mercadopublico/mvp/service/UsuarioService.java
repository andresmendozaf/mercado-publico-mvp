package com.mercadopublico.mvp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mercadopublico.mvp.exception.RecursoDuplicadoException;
import com.mercadopublico.mvp.exception.RecursoNoEncontradoException;
import com.mercadopublico.mvp.model.Usuario;
import com.mercadopublico.mvp.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario registrarUsuario(Usuario usuario) {
        // Regla de negocio: Validar que no exista el RUN/ID
        if (usuarioRepository.findByRunOId(usuario.getRunOId()).isPresent()) {
            throw new RecursoDuplicadoException("El RUN/ID fiscal ya está registrado.");
        }
        // Validar que no exista el email
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RecursoDuplicadoException("El correo electrónico ya está registrado.");
        }
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con el ID: " + id));
    }
}
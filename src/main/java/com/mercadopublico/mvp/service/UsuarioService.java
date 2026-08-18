package com.mercadopublico.mvp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mercadopublico.mvp.dto.UsuarioResponseDTO;
import com.mercadopublico.mvp.exception.RecursoDuplicadoException;
import com.mercadopublico.mvp.exception.RecursoNoEncontradoException;
import com.mercadopublico.mvp.mapper.UsuarioMapper;
import com.mercadopublico.mvp.model.Usuario;
import com.mercadopublico.mvp.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    public UsuarioResponseDTO registrarUsuario(Usuario usuario) {
        // Regla de negocio: Validar que no exista el RUN/ID
        if (usuarioRepository.findByRunOId(usuario.getRunOId()).isPresent()) {
            throw new RecursoDuplicadoException("El RUN/ID fiscal ya está registrado.");
        }
        // Validar que no exista el email
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RecursoDuplicadoException("El correo electrónico ya está registrado.");
        }
        return usuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    public List<UsuarioResponseDTO> obtenerTodos() {
        return usuarioMapper.toResponseDTOList(usuarioRepository.findAll());
    }

    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con el ID: " + id));
        return usuarioMapper.toResponseDTO(usuario);
    }
}
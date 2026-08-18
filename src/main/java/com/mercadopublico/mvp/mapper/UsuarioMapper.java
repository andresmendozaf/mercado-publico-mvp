package com.mercadopublico.mvp.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.mercadopublico.mvp.dto.UsuarioDTO;
import com.mercadopublico.mvp.dto.UsuarioResponseDTO;
import com.mercadopublico.mvp.model.Usuario;

@Component
public class UsuarioMapper {

    /**
     * Convierte el DTO de entrada a una entidad de persistencia JPA.
     */
    public Usuario toEntity(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setRunOId(dto.runOId());
        usuario.setNombre(dto.nombre());
        usuario.setEmail(dto.email());
        usuario.setRol(dto.rol());
        return usuario;
    }

    /**
     * Convierte una Entidad JPA al contrato de salida del API.
     */
    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getRunOId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getFechaCreacion()
        );
    }

    public List<UsuarioResponseDTO> toResponseDTOList(List<Usuario> usuarios) {
        return usuarios.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

package com.mercadopublico.mvp.dto;

import com.mercadopublico.mvp.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UsuarioDTO(

    @NotBlank(message = "El RUN/ID fiscal es obligatorio.")
    String runOId,

    @NotBlank(message = "El nombre es obligatorio.")
    String nombre,

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El formato del correo electrónico no es válido.")
    String email,

    @NotBlank(message = "El rol es obligatorio.")
    @Pattern(regexp = "^(COMPRADOR|PROVEEDOR|ADMIN)$", message = "El rol debe ser COMPRADOR, PROVEEDOR o ADMIN.")
    String rol
) {
    /**
     * Mapea el DTO inmutable a una entidad de persistencia JPA.
     */
    public Usuario toEntity() {
        Usuario usuario = new Usuario();
        usuario.setRunOId(this.runOId());
        usuario.setNombre(this.nombre());
        usuario.setEmail(this.email());
        usuario.setRol(this.rol());
        return usuario;
    }
}
package com.mercadopublico.mvp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mercadopublico.mvp.dto.UsuarioDTO;
import com.mercadopublico.mvp.model.Usuario;
import com.mercadopublico.mvp.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

   private final UsuarioService usuarioService;

    // Inyección por constructor recomendada
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Endpoint para registrar un nuevo usuario utilizando un DTO inmutable.
     * Retorna HTTP Status 201 (Created).
     */
    @PostMapping
    public ResponseEntity<Usuario> registrarUsuario(@RequestBody UsuarioDTO dto) {
        // Mapeo manual del DTO a la Entidad de persistencia
        Usuario usuario = new Usuario();
        usuario.setRunOId(dto.runOId());
        usuario.setNombre(dto.nombre());
        usuario.setEmail(dto.email());
        usuario.setRol(dto.rol());
        
        Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }

    /**
     * Endpoint para listar todos los usuarios del sistema.
     * Retorna HTTP Status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodos() {
        List<Usuario> usuarios = usuarioService.obtenerTodos();
        return ResponseEntity.ok(usuarios);
    }
}
package com.mercadopublico.mvp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mercadopublico.mvp.dto.UsuarioDTO;
import com.mercadopublico.mvp.model.Usuario;
import com.mercadopublico.mvp.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Endpoint para registrar un nuevo usuario utilizando un DTO inmutable.
     * Retorna HTTP Status 201 (Created).
     */
    @PostMapping
    public ResponseEntity<Usuario> registrarUsuario(@Valid @RequestBody UsuarioDTO dto) {
        Usuario nuevoUsuario = usuarioService.registrarUsuario(dto.toEntity());
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

    /**
     * Endpoint para obtener un usuario por su ID.
     * Retorna HTTP Status 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(usuario);
    }
}
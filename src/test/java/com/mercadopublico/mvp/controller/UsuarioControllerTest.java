package com.mercadopublico.mvp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopublico.mvp.dto.UsuarioDTO;
import com.mercadopublico.mvp.exception.RecursoDuplicadoException;
import com.mercadopublico.mvp.exception.RecursoNoEncontradoException;
import com.mercadopublico.mvp.model.Usuario;
import com.mercadopublico.mvp.service.UsuarioService;

@WebMvcTest(UsuarioController.class) // Levanta solo el contexto web de este controlador
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simula las peticiones HTTP

    @Autowired
    private ObjectMapper objectMapper; // Convierte objetos Java a JSON y viceversa

    @MockBean
    private UsuarioService usuarioService; // Simulamos el servicio (la lógica de negocio ya se probó en la Parte A)

    private Usuario usuarioRespuesta;
    private UsuarioDTO usuarioPeticion;

    @BeforeEach
    void setUp() {
        // Objeto que simula lo que el cliente (React) nos envía
        usuarioPeticion = new UsuarioDTO("11222333-4", "Juan Pérez", "juan@correo.cl", "COMPRADOR");

        // Objeto que simula lo que devuelve la base de datos (con ID)
        usuarioRespuesta = new Usuario();
        usuarioRespuesta.setId(1L);
        usuarioRespuesta.setRunOId("11222333-4");
        usuarioRespuesta.setNombre("Juan Pérez");
        usuarioRespuesta.setEmail("juan@correo.cl");
        usuarioRespuesta.setRol("COMPRADOR");
    }

    @Test
    @DisplayName("POST /api/usuarios - Debe retornar 201 Created al guardar usuario")
    void registrarUsuario_Exito() throws Exception {
        // Simulamos la respuesta del servicio
        when(usuarioService.registrarUsuario(any(Usuario.class))).thenReturn(usuarioRespuesta);

        // Realizamos la petición HTTP simulada
        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioPeticion)))
                // Verificamos el HTTP Status
                .andExpect(status().isCreated())
                // Verificamos que el JSON de respuesta tenga los datos correctos
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"));
    }

    @Test
    @DisplayName("POST /api/usuarios - Debe retornar 400 Bad Request si faltan datos en el DTO")
    void registrarUsuario_FallaValidacion() throws Exception {
        // Enviamos un DTO con el email vacío (rompe la validación @NotBlank)
        UsuarioDTO dtoInvalido = new UsuarioDTO("11222333-4", "Juan Pérez", "", "COMPRADOR");

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoInvalido)))
                // Spring Boot Validation debería interceptarlo antes de llegar al servicio
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/usuarios - Debe retornar 409 Conflict si hay datos duplicados")
    void registrarUsuario_FallaDuplicado() throws Exception {
        // Simulamos que el servicio lanza la excepción
        when(usuarioService.registrarUsuario(any(Usuario.class)))
                .thenThrow(new RecursoDuplicadoException("El correo ya existe"));

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioPeticion)))
                // El GlobalExceptionController lo traduce a 409
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/usuarios - Debe retornar 200 OK y la lista de usuarios")
    void obtenerTodos_Exito() throws Exception {
        when(usuarioService.obtenerTodos()).thenReturn(List.of(usuarioRespuesta));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                // Verificamos que es un Array JSON de tamaño 1
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].email").value("juan@correo.cl"));
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Debe retornar 200 OK al encontrar el usuario")
    void obtenerPorId_Exito() throws Exception {
        when(usuarioService.obtenerPorId(1L)).thenReturn(usuarioRespuesta);

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"));
    }

    @Test
    @DisplayName("GET /api/usuario/{id} - Debe retornar 404 Not Found si no existe")
    void obtenerPorId_FallaNoEncontrado() throws Exception {
        when(usuarioService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Usuario no encontrado"));

        mockMvc.perform(get("/api/usuario/99"))
                .andExpect(status().isNotFound());
    }
}
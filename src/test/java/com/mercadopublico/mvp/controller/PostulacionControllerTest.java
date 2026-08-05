package com.mercadopublico.mvp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopublico.mvp.dto.PostulacionDTO;
import com.mercadopublico.mvp.model.EstadoPostulacion;
import com.mercadopublico.mvp.service.PostulacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostulacionController.class)
class PostulacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostulacionService postulacionService;

    private PostulacionDTO postulacionDTO;

    @BeforeEach
    void setUp() {
        postulacionDTO = new PostulacionDTO(
                1L,
                150000.0,
                "Propuesta técnica detallada para licitación de insumos",
                EstadoPostulacion.POR_ESTUDIAR,
                10L,
                5L
        );
    }

    @Test
    @DisplayName("POST /api/postulaciones - Debe crear una postulación y retornar 201 Created")
    void crearPostulacion_DebeRetornar201() throws Exception {
        when(postulacionService.crearPostulacion(any(PostulacionDTO.class))).thenReturn(postulacionDTO);

        mockMvc.perform(post("/api/postulaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postulacionDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.montoPostulacion").value(150000.0))
                .andExpect(jsonPath("$.licitacionId").value(10L))
                .andExpect(jsonPath("$.proveedorId").value(5L))
                .andExpect(jsonPath("$.estado").value("POR_ESTUDIAR"));
    }

    @Test
    @DisplayName("GET /api/postulaciones/usuario/{proveedorId} - Debe retornar lista de postulaciones")
    void obtenerPorProveedor_DebeRetornarLista() throws Exception {
        when(postulacionService.obtenerPorProveedor(5L)).thenReturn(List.of(postulacionDTO));

                mockMvc.perform(get("/api/postulaciones/usuario/5"))                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].proveedorId").value(5L));
    }

    @Test
    @DisplayName("GET /api/postulaciones/usuario/{proveedorId}/estado - Debe filtrar por estado")
    void obtenerPorProveedorYEstado_DebeRetornarListaFiltrada() throws Exception {
        when(postulacionService.obtenerPorProveedorYEstado(5L, EstadoPostulacion.POR_ESTUDIAR))
                .thenReturn(List.of(postulacionDTO));

        mockMvc.perform(get("/api/postulaciones/usuario/5/estado")
                        .param("estado", "POR_ESTUDIAR"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("POR_ESTUDIAR"));
    }

    @Test
    @DisplayName("PATCH /api/postulaciones/{id}/estado - Debe actualizar el estado")
    void cambiarEstado_DebeRetornarDTOActualizado() throws Exception {
        PostulacionDTO dtoActualizado = new PostulacionDTO(
                1L, 150000.0, "Propuesta técnica", EstadoPostulacion.EN_PREPARACION, 10L, 5L
        );

        when(postulacionService.cambiarEstado(1L, EstadoPostulacion.EN_PREPARACION))
                .thenReturn(dtoActualizado);

        mockMvc.perform(patch("/api/postulaciones/1/estado")
                        .param("nuevoEstado", "EN_PREPARACION"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PREPARACION"));
    }

    @Test
    @DisplayName("DELETE /api/postulaciones/{id} - Debe eliminar y retornar 204 No Content")
    void eliminarPostulacion_DebeRetornar204() throws Exception {
        doNothing().when(postulacionService).eliminarPostulacion(1L);

        mockMvc.perform(delete("/api/postulaciones/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
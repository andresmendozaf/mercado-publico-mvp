package com.mercadopublico.mvp.controller;

import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.service.LicitacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LicitacionController.class)
class LicitacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LicitacionService licitacionService;

    private Licitacion licitacionEjemplo;

    @BeforeEach
    void setUp() {
        licitacionEjemplo = new Licitacion();
        licitacionEjemplo.setId(1L);
        licitacionEjemplo.setCodigoExterno("1234-56-78");
        licitacionEjemplo.setNombre("Adquisición de Servidores de Prueba");
        licitacionEjemplo.setEstado(EstadoLicitacion.PUBLICADA);
    }

    @Test
    @DisplayName("GET /api/licitaciones - Debe retornar lista de todas las licitaciones con estatus HTTP 200 OK")
    void obtenerTodas_DebeRetornarLicitaciones() throws Exception {
        when(licitacionService.obtenerTodas()).thenReturn(List.of(licitacionEjemplo));

        mockMvc.perform(get("/api/licitaciones")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoExterno").value("1234-56-78"))
                .andExpect(jsonPath("$[0].nombre").value("Adquisición de Servidores de Prueba"));
    }

    @Test
    @DisplayName("GET /api/licitaciones/abiertas - Debe retornar solo licitaciones abiertas con estatus HTTP 200 OK")
    void obtenerLicitacionesAbiertas_DebeRetornarLista() throws Exception {
        when(licitacionService.obtenerLicitacionesAbiertas()).thenReturn(List.of(licitacionEjemplo));

        mockMvc.perform(get("/api/licitaciones/abiertas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PUBLICADA"));
    }

    @Test
    @DisplayName("POST /api/licitaciones/sincronizar - Debe gatillar la ingesta e indicar éxito 200 OK")
    void sincronizarLicitaciones_DebeRetornarExito() throws Exception {
        when(licitacionService.sincronizarLicitacionesDelDia()).thenReturn(List.of(licitacionEjemplo));

        mockMvc.perform(post("/api/licitaciones/sincronizar")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoExterno").value("1234-56-78"));
    }
}
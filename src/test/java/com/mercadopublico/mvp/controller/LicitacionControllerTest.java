package com.mercadopublico.mvp.controller;

import com.mercadopublico.mvp.dto.LicitacionResponseDTO;
import com.mercadopublico.mvp.model.EstadoLicitacion;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

    private LicitacionResponseDTO licitacionEjemplo;

    @BeforeEach
    void setUp() {
        licitacionEjemplo = new LicitacionResponseDTO(
                1L,
                "1234-56-78",
                "Adquisición de Servidores de Prueba",
                null,
                null,
                EstadoLicitacion.PUBLICADA,
                null,
                null,
                null,
                null,
                null);
    }

    @Test
    @DisplayName("GET /api/licitaciones - Sin parámetros debe conservar el comportamiento actual (retorna todas)")
    void obtenerTodas_SinParametros_DebeRetornarLicitaciones() throws Exception {
        when(licitacionService.buscarConFiltros(isNull(), isNull(), isNull()))
                .thenReturn(List.of(licitacionEjemplo));

        mockMvc.perform(get("/api/licitaciones")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoExterno").value("1234-56-78"))
                .andExpect(jsonPath("$[0].nombre").value("Adquisición de Servidores de Prueba"));
    }

    @Test
    @DisplayName("GET /api/licitaciones?texto=... - Debe delegar el filtro de texto al service")
    void obtenerTodas_ConFiltroTexto_DebeRetornarLicitacionesFiltradas() throws Exception {
        when(licitacionService.buscarConFiltros(eq("Servidores"), isNull(), isNull()))
                .thenReturn(List.of(licitacionEjemplo));

        mockMvc.perform(get("/api/licitaciones").param("texto", "Servidores")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Adquisición de Servidores de Prueba"));
    }

    @Test
    @DisplayName("GET /api/licitaciones?estado=... - Debe delegar el filtro de estado al service")
    void obtenerTodas_ConFiltroEstado_DebeRetornarLicitacionesFiltradas() throws Exception {
        when(licitacionService.buscarConFiltros(isNull(), eq(EstadoLicitacion.PUBLICADA), isNull()))
                .thenReturn(List.of(licitacionEjemplo));

        mockMvc.perform(get("/api/licitaciones").param("estado", "PUBLICADA")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PUBLICADA"));
    }

    @Test
    @DisplayName("GET /api/licitaciones?organismo=... - Debe delegar el filtro de organismo al service")
    void obtenerTodas_ConFiltroOrganismo_DebeRetornarLicitacionesFiltradas() throws Exception {
        when(licitacionService.buscarConFiltros(isNull(), isNull(), eq("Defensa")))
                .thenReturn(List.of(licitacionEjemplo));

        mockMvc.perform(get("/api/licitaciones").param("organismo", "Defensa")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoExterno").value("1234-56-78"));
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
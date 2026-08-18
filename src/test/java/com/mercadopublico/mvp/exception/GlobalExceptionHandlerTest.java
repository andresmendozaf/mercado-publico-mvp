package com.mercadopublico.mvp.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("RecursoNoEncontradoException -> 404 con detalle")
    void manejarRecursoNoEncontrado_Retorna404() {
        ProblemDetail problem = handler.manejarRecursoNoEncontrado(
                new RecursoNoEncontradoException("Usuario no encontrado con el ID: 99"));

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Recurso no encontrado", problem.getTitle());
        assertEquals("Usuario no encontrado con el ID: 99", problem.getDetail());
        assertNotNull(problem.getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("RecursoDuplicadoException -> 409 con detalle")
    void manejarRecursoDuplicado_Retorna409() {
        ProblemDetail problem = handler.manejarRecursoDuplicado(
                new RecursoDuplicadoException("El recurso ya existe"));

        assertEquals(HttpStatus.CONFLICT.value(), problem.getStatus());
        assertEquals("El recurso ya existe", problem.getDetail());
    }

    @Test
    @DisplayName("ReglaNegocioException -> 400 con detalle")
    void manejarReglaNegocio_Retorna400() {
        ProblemDetail problem = handler.manejarReglaNegocio(
                new ReglaNegocioException("Datos inválidos en la petición"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Datos inválidos en la petición", problem.getDetail());
    }

    @Test
    @DisplayName("IllegalArgumentException -> 400 (red de seguridad)")
    void manejarIllegalArgument_Retorna400() {
        ProblemDetail problem = handler.manejarReglaNegocio(
                new IllegalArgumentException("Postulación no encontrada: 5"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Postulación no encontrada: 5", problem.getDetail());
    }

    @Test
    @DisplayName("IntegracionApiException -> 502 SIN exponer la causa raíz al cliente")
    void manejarIntegracionApi_Retorna502SinFiltrarCausa() {
        Throwable causaRaiz = new RuntimeException("Timeout de conexión al servidor");
        ProblemDetail problem = handler.manejarIntegracionApi(
                new IntegracionApiException("Fallo en API externa", causaRaiz));

        assertEquals(HttpStatus.BAD_GATEWAY.value(), problem.getStatus());
        assertEquals("Fallo en API externa", problem.getDetail());
        // El detalle interno de la causa no debe filtrarse en la respuesta.
        assertFalse(problem.getProperties().containsKey("detalle"));
        assertFalse(problem.getDetail().contains("Timeout"));
    }

    @Test
    @DisplayName("Exception no controlada -> 500 con mensaje genérico (sin filtrar internals)")
    void manejarGenerico_Retorna500() {
        ProblemDetail problem = handler.manejarGenerico(
                new RuntimeException("NullPointer en el repositorio X"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
        assertEquals("Error interno", problem.getTitle());
        // No se expone el mensaje interno de la excepción.
        assertFalse(problem.getDetail().contains("NullPointer"));
    }
}

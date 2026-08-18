package com.mercadopublico.mvp.exception;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Manejo global de errores del API.
 *
 * <p>Todas las respuestas de error se emiten como {@link ProblemDetail}
 * (application/problem+json, RFC 9457), con un formato uniforme:
 * {@code status}, {@code title}, {@code detail}, {@code timestamp} y, cuando
 * aplica, propiedades extra como {@code camposInvalidos}.</p>
 *
 * <p>Extiende {@link ResponseEntityExceptionHandler} para heredar el manejo
 * en formato ProblemDetail de las excepciones propias del framework
 * (cuerpo ilegible, tipo de parámetro inválido, ruta inexistente, etc.).</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ---------------- Excepciones de dominio ----------------

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ProblemDetail manejarRecursoDuplicado(RecursoDuplicadoException ex) {
        return construir(HttpStatus.CONFLICT, "Recurso duplicado", ex.getMessage());
    }

    @ExceptionHandler({ReglaNegocioException.class, IllegalArgumentException.class})
    public ProblemDetail manejarReglaNegocio(RuntimeException ex) {
        return construir(HttpStatus.BAD_REQUEST, "Regla de negocio", ex.getMessage());
    }

    @ExceptionHandler(IntegracionApiException.class)
    public ProblemDetail manejarIntegracionApi(IntegracionApiException ex) {
        // La causa raíz se registra en el log, nunca se expone al cliente.
        log.error("Fallo de integración con API externa", ex);
        return construir(HttpStatus.BAD_GATEWAY, "Error de integración",
                ex.getMessage());
    }

    // ---------------- Fallback ----------------

    @ExceptionHandler(Exception.class)
    public ProblemDetail manejarGenerico(Exception ex) {
        log.error("Error no controlado", ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado. Contacte al administrador.");
    }

    // ---------------- Validación de @Valid ----------------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemDetail problem = construir(HttpStatus.BAD_REQUEST, "Error de validación",
                "Uno o más campos de la petición son inválidos.");

        Map<String, String> camposInvalidos = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            camposInvalidos.put(error.getField(), error.getDefaultMessage());
        }
        problem.setProperty("camposInvalidos", camposInvalidos);

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    // ---------------- Helper ----------------

    private ProblemDetail construir(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("about:blank"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}

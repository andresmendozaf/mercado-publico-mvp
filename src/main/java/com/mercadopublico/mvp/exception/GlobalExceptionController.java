package com.mercadopublico.mvp.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionController {

    private static final String KEY_ERROR = "error";
    private static final String KEY_DETALLE = "detalle";

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<Map<String, String>> manejarRecursoDuplicado(RecursoDuplicadoException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put(KEY_ERROR, ex.getMessage());
        return new ResponseEntity<>(respuesta, HttpStatus.CONFLICT); // HTTP 409 Conflict
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, String>> manejarReglaNegocio(ReglaNegocioException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put(KEY_ERROR, ex.getMessage());
        return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST); // HTTP 400 Bad Request
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IntegracionApiException.class)
    public ResponseEntity<Map<String, String>> manejarIntegracionApi(IntegracionApiException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put(KEY_ERROR, ex.getMessage());
        // Buscamos la causa raíz para dar más detalle si es necesario
        if (ex.getCause() != null) {
            respuesta.put(KEY_DETALLE, ex.getCause().getMessage());
        }
        return new ResponseEntity<>(respuesta, HttpStatus.BAD_GATEWAY); // HTTP 502
    }

}

package com.mercadopublico.mvp.exception;

public class RecursoNoEncontradoException extends RuntimeException {
    
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
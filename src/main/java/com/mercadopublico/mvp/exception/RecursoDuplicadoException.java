package com.mercadopublico.mvp.exception;

public class RecursoDuplicadoException extends RuntimeException{

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }

}

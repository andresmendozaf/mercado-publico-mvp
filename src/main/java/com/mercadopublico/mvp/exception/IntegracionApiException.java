package com.mercadopublico.mvp.exception;

public class IntegracionApiException extends RuntimeException {

    public IntegracionApiException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
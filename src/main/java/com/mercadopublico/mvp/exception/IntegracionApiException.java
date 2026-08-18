package com.mercadopublico.mvp.exception;

public class IntegracionApiException extends RuntimeException {

    // Constructor 1: Solo con el mensaje (Es el que estaba pidiendo el test)
    public IntegracionApiException(String mensaje) {
        super(mensaje);
    }

    // Constructor 2: Con el mensaje y la causa raíz (El que ya tenías)
    public IntegracionApiException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
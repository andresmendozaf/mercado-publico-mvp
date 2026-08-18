package com.mercadopublico.mvp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercadopublico.api")
public record MercadoPublicoProperties(
        String baseUrl,
        String ticket
) {
}
package com.mercadopublico.mvp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient chileCompraRestClient(
            RestClient.Builder builder,
            MercadoPublicoProperties properties) {

        return builder
                .baseUrl(properties.baseUrl())
                .build();
    }
}
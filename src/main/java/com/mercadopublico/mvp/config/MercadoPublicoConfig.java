package com.mercadopublico.mvp.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MercadoPublicoProperties.class)
public class MercadoPublicoConfig {
}
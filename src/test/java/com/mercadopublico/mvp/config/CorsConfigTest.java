package com.mercadopublico.mvp.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CorsConfigTest {

    @Test
    @DisplayName("Debe configurar las reglas de CORS usando la lista de orígenes")
    void testCorsConfigurer() {
        CorsConfig corsConfig = new CorsConfig();

        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", List.of("http://localhost:5173", "http://localhost:3000"));

        WebMvcConfigurer configurer = corsConfig.corsConfigurer();
        assertNotNull(configurer, "El Bean WebMvcConfigurer no debe ser nulo");

        CorsRegistry registry = new CorsRegistry();
        configurer.addCorsMappings(registry);
    }
}
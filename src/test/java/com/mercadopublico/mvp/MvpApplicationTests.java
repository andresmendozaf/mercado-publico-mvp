package com.mercadopublico.mvp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MvpApplicationMainTest {

    @Test
    void main() {
        assertDoesNotThrow(() ->
                new SpringApplicationBuilder(MvpApplication.class)
                        .profiles("test")
                        .run()
                        .close()
        );
    }
}
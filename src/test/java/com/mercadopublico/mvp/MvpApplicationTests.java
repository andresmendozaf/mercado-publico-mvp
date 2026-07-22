package com.mercadopublico.mvp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "MERCADO_PUBLICO_TICKET=test-ticket-dummy",
    "spring.profiles.active=test"
})class MvpApplicationTests {

	@Test
	void contextLoads() {
	}

}

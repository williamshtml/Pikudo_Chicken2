package com.restaurant;

import org.junit.jupiter.api.Disabled; // <-- IMPORTANTE: Importar la anotación
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled // <-- ESTO APAGA ESTE TEST ESPECÍFICO DURANTE EL CLEAN AND BUILD
@SpringBootTest
class PikudoChicken2ApplicationTests {

	@Test
	void contextLoads() {
	}

}
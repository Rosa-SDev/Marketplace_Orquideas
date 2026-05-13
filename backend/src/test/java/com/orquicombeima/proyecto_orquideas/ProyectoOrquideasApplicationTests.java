package com.orquicombeima.proyecto_orquideas;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requiere BD MySQL viva y variables de entorno; deshabilitado en tests automatizados. " +
		"Si se quiere un sanity check real, hacer un @SpringBootTest aparte con perfil test + H2.")
class ProyectoOrquideasApplicationTests {

	@Test
	void contextLoads() {
	}

}
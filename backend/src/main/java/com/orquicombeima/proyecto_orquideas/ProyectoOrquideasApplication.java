package com.orquicombeima.proyecto_orquideas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling activa el procesamiento de las anotaciones @Scheduled
// Sin esto, el ReservaScheduler nunca se ejecutaría automáticamente
@SpringBootApplication
@EnableScheduling
public class ProyectoOrquideasApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProyectoOrquideasApplication.class, args);
	}

}
package com.example.sistemaMicroservicios;

import org.springframework.boot.SpringApplication;

public class TestSistemaMicroserviciosApplication {

	public static void main(String[] args) {
		SpringApplication.from(SistemaMicroserviciosApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

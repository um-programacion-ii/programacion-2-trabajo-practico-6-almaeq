package com.example.sistemaMicroservicios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SistemaMicroserviciosApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaMicroserviciosApplication.class, args);
	}

}

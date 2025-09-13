package com.example.dataService;

import org.springframework.boot.SpringApplication;

public class TestDataServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(com.example.dataService.DataServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

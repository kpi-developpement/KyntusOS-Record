package com.kyntus.operatingsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OperatingsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(OperatingsystemApplication.class, args);
	}

	// 🔥 THE V8 FIX: N-creyiw l'ObjectMapper l'Spring Boot bach y-t-injekta f l'Batch Processor
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
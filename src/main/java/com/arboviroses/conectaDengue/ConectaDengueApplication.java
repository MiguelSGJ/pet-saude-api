package com.arboviroses.conectaDengue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ConectaDengueApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConectaDengueApplication.class, args);
	}
}

package com.posthaste;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ShipmentApplication {
	public static void main(String[] args) {
		SpringApplication.run(ShipmentApplication.class, args);
	}
}

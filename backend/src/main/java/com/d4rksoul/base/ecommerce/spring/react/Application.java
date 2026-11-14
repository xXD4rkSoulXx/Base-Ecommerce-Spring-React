package com.d4rksoul.base.ecommerce.spring.react;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {

	static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

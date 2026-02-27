package com.aprenderSpring.ecommerceSpring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class EcommerceSpringApplication {
	static void main(String[] args) {
        /*
        *   Instalar as dependências:
        *       - Spring Web
        *       - Lombok
        *       - MySQL Driver ou PostgreSQL Driver
        *       - Spring Data JPA
        *       - Spring Security
        *       - Validation
        * */

        /*
        *   Meter a dependência do cdimascio, tanto no import, tanto no pom.xml,
        * para poder usar o .env
        *
        *   Configurar o ./src/main/resources/application.properties
        * */


		Dotenv dotenv = Dotenv.load();

        System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("DB_NAME", dotenv.get("DB_NAME"));
		System.setProperty("DB_USER", dotenv.get("DB_USER"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

		SpringApplication.run(EcommerceSpringApplication.class, args);
	}

}

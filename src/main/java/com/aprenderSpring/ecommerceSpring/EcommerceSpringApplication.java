package com.aprenderSpring.ecommerceSpring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class EcommerceSpringApplication {
	public static void main(String[] args) {
        /*
        * Instalar as dependências:
        * - Spring Web
        * - Lombok
        * - MySQL Driver ou PostgreSQL Driver
        * - Spring Data JPA
        * - Spring Security
        * - Validation
        * */

        /*
        * Meter a dependência do cdimascio para poder usar o .env
        * */


		Dotenv dotenv = Dotenv.load();

		System.setProperty("DB_HOST", dotenv.get("DB_HOST"));
		System.setProperty("DB_PORT", dotenv.get("DB_PORT"));
		System.setProperty("DB_NAME", dotenv.get("DB_NAME"));
		System.setProperty("DB_USER", dotenv.get("DB_USER"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

		SpringApplication.run(EcommerceSpringApplication.class, args);
	}

}

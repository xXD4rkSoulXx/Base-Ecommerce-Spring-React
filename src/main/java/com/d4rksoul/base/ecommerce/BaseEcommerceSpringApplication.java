package com.d4rksoul.base.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class BaseEcommerceSpringApplication {
	static void main(String[] args) {
        /*
        *   Instalar as dependências:
        *       - Spring Web
        *       - Lombok
        *       - PostgreSQL Driver ou MySQL Driver
        *       - Spring Data JPA
        *       - Spring Security
        *       - Validation
        * */

        /*
        *   Meter a dependência do cdimascio, tanto no import, tanto no pom.xml,
        * para poder usar o .env
        *
        * 	Meter a dependência springdoc openapi starter webmvc ui, para implementar
        * o Swagger e ter a documentação automática das APIs.
        *
        * 	Meter a dependência mapstruct e mapstruct processor, para fazer os
        * mappers automáticos.
        *
        * 	Meter a dependência jjwt api, jjwt impl e jjwt jackson, para implementar
        * o jwt.
        *
        *   Configurar o ./src/main/resources/application.yml
        * */


		Dotenv dotenv = Dotenv.configure().directory("./").load();

        System.setProperty("DB_HOST", dotenv.get("DB_HOST"));
		System.setProperty("DB_PORT", dotenv.get("DB_PORT"));
		System.setProperty("DB_NAME", dotenv.get("DB_NAME"));
		System.setProperty("DB_USER", dotenv.get("DB_USER"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

		System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
		System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION"));

		SpringApplication.run(BaseEcommerceSpringApplication.class, args);
	}

}

package com.jgmedellin.concept_coffee_shop

import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@OpenAPIDefinition(
	info = Info(
		title = "Concept Coffee Shop REST API",
		version = "v1",
		description = "REST API for a concept coffee shop app, designed during the Google UX/UI certificate program.",
		contact = Contact(
			name = "Gerardo Medellin",
			email = "thegera4@hotmail.com",
			url = "https://www.jgmedellin.com"
		)
	),
	externalDocs = ExternalDocumentation(
		description = "Github Repository",
		url = "https://github.com/thegera4/concept-coffee-shop",
	)
)
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
class CoffeeShopApplication

fun main(args: Array<String>) {
	runApplication<CoffeeShopApplication>(*args)
}
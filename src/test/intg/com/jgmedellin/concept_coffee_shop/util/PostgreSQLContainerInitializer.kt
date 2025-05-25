package com.jgmedellin.concept_coffee_shop.util

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
open class PostgreSQLContainerInitializer {
    companion object {
        @Container // This will start the PostgreSQL container for the tests
        val postgresDB = PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres:latest")).apply {
            withDatabaseName("coffee-shop")
            withUsername("Postgres")
            withPassword("Postgres")
        }

        @JvmStatic // This will set the properties for the Spring application context
        @DynamicPropertySource // This will set the properties for the Spring application context
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresDB::getJdbcUrl)
            registry.add("spring.datasource.username", postgresDB::getUsername)
            registry.add("spring.datasource.password", postgresDB::getPassword)
        }
    }
}
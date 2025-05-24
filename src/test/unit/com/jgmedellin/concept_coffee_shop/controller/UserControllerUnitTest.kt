package com.jgmedellin.concept_coffee_shop.controller

import com.jgmedellin.concept_cofee_shop.controller.UserController
import com.jgmedellin.concept_coffee_shop.dto.UserDTO
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import com.jgmedellin.concept_coffee_shop.service.UserService
import com.jgmedellin.concept_coffee_shop.util.JwtUtil
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.ResponseEntity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.web.reactive.server.WebTestClient

@WebMvcTest(controllers = [UserController::class])
@AutoConfigureWebTestClient
class UserControllerUnitTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @MockkBean
    lateinit var userServiceMock: UserService

    @MockkBean
    lateinit var jwtUtil: JwtUtil

    @TestConfiguration
    class SecurityTestConfig {
        @Bean
        fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
            return http
                .csrf { it.disable() }
                .authorizeHttpRequests { auth -> auth.anyRequest().permitAll() } // Allow all requests for testing
                .build()
        }
    }

    @Test
    fun registerUser() {
        val userDTO = UserDTO("test2@email.com", "Pass123#")
        val response = GeneralResponse(201, "User registered successfully.", null)
        every { userServiceMock.registerUser(userDTO) } returns ResponseEntity.status(201).body(response)

        webTestClient.post()
            .uri("/api/v1/users/register")
            .bodyValue(userDTO)
            .exchange()
            .expectStatus().isCreated
            .expectBody(GeneralResponse::class.java)
            .isEqualTo(response) // Assert the response is as expected
    }

}
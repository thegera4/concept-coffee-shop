package com.jgmedellin.concept_coffee_shop.controller

import com.jgmedellin.concept_coffee_shop.constants.UserRoles
import com.jgmedellin.concept_coffee_shop.dto.NewRoleDTO
import com.jgmedellin.concept_coffee_shop.dto.UpdateUserDTO
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
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.web.reactive.server.WebTestClient

@Suppress("unused")
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

    @Test
    fun registerUserWithExistingEmail() {
        val userDTO = UserDTO("test@email.com", "Pass123#")
        val errorResponse = GeneralResponse (400, "User already exists.", null)

        every { userServiceMock.registerUser(userDTO) } returns
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)

        webTestClient.post()
            .uri("/api/v1/users/register")
            .bodyValue(userDTO)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(GeneralResponse::class.java)
            .isEqualTo(errorResponse)
    }

    @Test
    fun loginUser() {
        val userDTO = UserDTO("test@email.com", "Pass123#")
        val response = GeneralResponse(200, "User logged in successfully.", null)
        every { userServiceMock.loginUser(userDTO) } returns ResponseEntity.ok(response)

        webTestClient.post()
            .uri("/api/v1/users/login")
            .bodyValue(userDTO)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .isEqualTo(response)
    }

    @Test
    fun loginUserWithInvalidCredentials() {
        val userDTO = UserDTO("test@email.com", "Wr0ngPass#")
        val response = GeneralResponse(400, "BAD_REQUEST", "Invalid credentials.")
        every{userServiceMock.loginUser(userDTO)} returns ResponseEntity(response,HttpStatus.BAD_REQUEST)

        webTestClient.post()
            .uri("/api/v1/users/login")
            .bodyValue(userDTO)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.code").isEqualTo(400)
            .jsonPath("$.message").isEqualTo("BAD_REQUEST")
            .jsonPath("$.data").isEqualTo("Invalid credentials.")
    }

    @Test
    fun changeRole() {
        val newRoleDTO = NewRoleDTO("test2@email.com", UserRoles.ADMIN)
        val response = GeneralResponse(200, "User role changed successfully.", null)

        every { userServiceMock.changeRole(newRoleDTO) } returns ResponseEntity.ok(response)

        webTestClient.patch()
            .uri("/api/v1/users/changeRole")
            .bodyValue(newRoleDTO)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .isEqualTo(response)
    }

    @Test
    fun changeRoleWithInvalidEmail() {
        val newRoleDTO = NewRoleDTO("nonexistantemail@email.com", UserRoles.ADMIN)
        val errorResponse = GeneralResponse(404, "User does not exist.", null)

        every { userServiceMock.changeRole(newRoleDTO) } returns
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)

        webTestClient.patch()
            .uri("/api/v1/users/changeRole")
            .bodyValue(newRoleDTO)
            .exchange()
            .expectStatus().isNotFound
            .expectBody(GeneralResponse::class.java)
            .isEqualTo(errorResponse)
    }

    @Test
    fun getAllUsers() {
        val data = listOf(
            mapOf("id" to 1, "email" to "test@email.com", "role" to UserRoles.SUPER),
            mapOf("id" to 2, "email" to "test2@email.com", "role" to UserRoles.USER)
        )
        val response = GeneralResponse(200, "Users retrieved successfully.", data)
        every { userServiceMock.getAllUsers() } returns ResponseEntity.ok(response)

        webTestClient.get()
            .uri("/api/v1/users")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.code").isEqualTo(200)
            .jsonPath("$.message").isEqualTo("Users retrieved successfully.")
            .jsonPath("$.data[0].id").isEqualTo(1)
            .jsonPath("$.data[0].email").isEqualTo("test@email.com")
            .jsonPath("$.data[0].role").isEqualTo("SUPER")
            .jsonPath("$.data[1].id").isEqualTo(2)
            .jsonPath("$.data[1].email").isEqualTo("test2@email.com")
            .jsonPath("$.data[1].role").isEqualTo("USER")
    }

    @Test
    fun getUserById() {
        val userId = 1
        val response = GeneralResponse(200, "User retrieved successfully.", null)
        every { userServiceMock.getUserById(userId) } returns ResponseEntity.ok(response)

        webTestClient.get()
            .uri("/api/v1/users/$userId")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .isEqualTo(response)
    }

    @Test
    fun getUserByIdWithInvalidId() {
        val userId = 999
        val errorResponse = GeneralResponse(404, "User does not exist.", null)

        every { userServiceMock.getUserById(userId) } returns
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)

        webTestClient.get()
            .uri("/api/v1/users/$userId")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(GeneralResponse::class.java)
            .isEqualTo(errorResponse)
    }

    @Test
    fun deleteUser() {
        val userId = 1
        val response = GeneralResponse(200, "User deleted successfully.", null)
        every { userServiceMock.deleteUser(userId) } returns ResponseEntity.ok(response)

        webTestClient.delete()
            .uri("/api/v1/users/$userId")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .isEqualTo(response)
    }

    @Test
    fun deleteUserWithInvalidId() {
        val userId = 999
        val errorResponse = GeneralResponse(404, "User does not exist.", null)

        every { userServiceMock.deleteUser(userId) } returns
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)

        webTestClient.delete()
            .uri("/api/v1/users/$userId")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(GeneralResponse::class.java)
            .isEqualTo(errorResponse)
    }

    @Test
    fun updateUser() {
        val userId = 1
        val updateUserDTO = UpdateUserDTO(city = "Torreon", address = "Calle 123", phone = "1234567890")
        val response = GeneralResponse(200, "User updated successfully.", null)

        every { userServiceMock.updateUser(userId, updateUserDTO) } returns ResponseEntity.ok(response)

        webTestClient.put()
            .uri("/api/v1/users/$userId")
            .bodyValue(updateUserDTO)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .isEqualTo(response)
    }

    @Test
    fun updateUserWithInvalidId() {
        val userId = 999
        val updateUserDTO = UpdateUserDTO(city = "Torreon", address = "Calle 123", phone = "1234567890")
        val errorResponse = GeneralResponse(404, "User does not exist.", null)

        every { userServiceMock.updateUser(userId, updateUserDTO) } returns
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)

        webTestClient.put()
            .uri("/api/v1/users/$userId")
            .bodyValue(updateUserDTO)
            .exchange()
            .expectStatus().isNotFound
            .expectBody(GeneralResponse::class.java)
            .isEqualTo(errorResponse)
    }

    @Test
    fun updateUserWithInvalidData() {
        val userId = 1
        val updateUserDTO = UpdateUserDTO(city = "", address = null, phone = "1234567890")
        val errorResponse = GeneralResponse(400, "BAD_REQUEST", "Invalid data provided.")

        every { userServiceMock.updateUser(userId, updateUserDTO) } returns
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)

        webTestClient.put()
            .uri("/api/v1/users/$userId")
            .bodyValue(updateUserDTO)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(GeneralResponse::class.java)
            .isEqualTo(errorResponse)
    }
}
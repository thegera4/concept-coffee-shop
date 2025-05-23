package com.jgmedellin.concept_cofee_shop

import com.jgmedellin.concept_coffee_shop.CoffeeShopApplication
import com.jgmedellin.concept_coffee_shop.dto.UserDTO
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [CoffeeShopApplication::class]
)
@ActiveProfiles("dev")
@AutoConfigureWebTestClient
class UserController {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun registerUser () {
        val uniqueEmail = "test${System.currentTimeMillis()}@email.com"
        val userDTO = UserDTO(uniqueEmail, "Pass123#")

        val registeredUser = webTestClient.post()
            .uri("/api/v1/users/register")
            .bodyValue(userDTO)
            .exchange()
            .expectStatus().isCreated
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the user is registered successfully
        Assertions.assertNotNull(registeredUser)
        Assertions.assertEquals(201, registeredUser?.code)
        Assertions.assertEquals("User registered successfully.", registeredUser?.message)
        Assertions.assertNull(registeredUser?.data)
    }

    @Test
    fun login () {

    }

}
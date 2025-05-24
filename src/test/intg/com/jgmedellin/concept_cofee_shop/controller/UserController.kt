package com.jgmedellin.concept_cofee_shop.controller

import com.jgmedellin.concept_coffee_shop.CoffeeShopApplication
import com.jgmedellin.concept_coffee_shop.constants.UserRoles
import com.jgmedellin.concept_coffee_shop.dto.NewRoleDTO
import com.jgmedellin.concept_coffee_shop.dto.UpdateUserDTO
import com.jgmedellin.concept_coffee_shop.dto.UserDTO
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.collections.get

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
    fun registerUser() {
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
    fun login() {
        val userCredentials = UserDTO("test@email.com", "Pass123#")

        val loginResponse = webTestClient.post()
            .uri("/api/v1/users/login")
            .bodyValue(userCredentials)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the login is successful
        Assertions.assertEquals(200, loginResponse?.code)
        Assertions.assertEquals("User logged in successfully.", loginResponse?.message)
        Assertions.assertNotNull(loginResponse?.data)

        // Check if the data property contains a key called "token"
        val dataMap = loginResponse?.data as Map<*, *>
        Assertions.assertTrue(dataMap.containsKey("token"))

        // Extract the token property
        val token = dataMap["token"] as String

        // Check if the token is a valid JWT token
        val tokenParts = token.split(".")
        Assertions.assertEquals(3, tokenParts.size)
        tokenParts.forEach { part ->
            Assertions.assertTrue(part.isNotEmpty())
        }
    }

    @Test
    fun changeRole() {
        // First, login to get a JWT token from a superuser
        val superUserCredentials = UserDTO("test@email.com", "Pass123#")

        val loginResponse = webTestClient.post()
            .uri("/api/v1/users/login")
            .bodyValue(superUserCredentials)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Extract the JWT token
        val dataMap = loginResponse?.data as Map<*, *>
        val token = dataMap["token"] as String

        // Now use the token to change the role of another user
        val newRoleDTO = NewRoleDTO("test2@email.com", UserRoles.ADMIN)

        val changeRoleResponse = webTestClient.patch()
            .uri("/api/v1/users/changeRole")
            .header("Authorization", "Bearer $token")
            .bodyValue(newRoleDTO)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the role change is successful
        Assertions.assertEquals(200, changeRoleResponse?.code)
        Assertions.assertEquals("User role changed successfully.", changeRoleResponse?.message)
        Assertions.assertNotNull(changeRoleResponse?.data)

        // Check if the data property contains the updated user role
        val dataMapResponse = changeRoleResponse?.data as Map<*, *>
        Assertions.assertTrue(dataMapResponse.containsKey("role"))
        Assertions.assertEquals(UserRoles.ADMIN.toString(), dataMapResponse["role"])

        // After the role change, change it back to USER
        val revertRoleDTO = NewRoleDTO("test2@email.com", UserRoles.USER)
        val revertResponse = webTestClient.patch()
            .uri("/api/v1/users/changeRole")
            .header("Authorization", "Bearer $token")
            .bodyValue(revertRoleDTO)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the role revert is successful
        Assertions.assertEquals(200, revertResponse?.code)
        Assertions.assertEquals("User role changed successfully.", revertResponse?.message)
        Assertions.assertNotNull(revertResponse?.data)

        // Check if the data property contains the updated user role
        val revertDataMap = revertResponse?.data as Map<*, *>
        Assertions.assertTrue(revertDataMap.containsKey("role"))
        Assertions.assertEquals(UserRoles.USER.toString(), revertDataMap["role"])
    }

    @Test
    fun getAllUsers() {
        // First, login to get a JWT token from a user with SUPER or ADMIN role
        val superUserCredentials = UserDTO("test@email.com", "Pass123#")

        val loginResponse = webTestClient.post()
            .uri("/api/v1/users/login")
            .bodyValue(superUserCredentials)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Extract the JWT token
        val dataMap = loginResponse?.data as Map<*, *>
        val token = dataMap["token"] as String

        // Now use the token to get all users
        val allUsersResponse = webTestClient.get()
            .uri("/api/v1/users")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the response is successful
        Assertions.assertEquals(200, allUsersResponse?.code)
        Assertions.assertEquals("Users retrieved successfully.", allUsersResponse?.message)
        Assertions.assertNotNull(allUsersResponse?.data)

        // Check if the data property contains a list of users
        val dataMapResponse = allUsersResponse?.data as List<*>
        Assertions.assertTrue(dataMapResponse.isNotEmpty())
        Assertions.assertTrue(dataMapResponse.all { it is Map<*, *> })
        dataMapResponse.forEach { user ->
            val userMap = user as Map<*, *>
            Assertions.assertTrue(userMap.containsKey("email"))
            Assertions.assertTrue(userMap.containsKey("role"))
            Assertions.assertTrue(userMap["email"] is String)
            Assertions.assertTrue(userMap["role"] is String)
        }
    }

    @Test
    fun getUserById() {
        // First, login to get a JWT token from a user with SUPER or ADMIN role
        val superUserCredentials = UserDTO("test@email.com", "Pass123#")

        val loginResponse = webTestClient.post()
            .uri("/api/v1/users/login")
            .bodyValue(superUserCredentials)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Extract the JWT token
        val dataMap = loginResponse?.data as Map<*, *>
        val token = dataMap["token"] as String

        // Now use the token to get a user by ID (make sure the user exists in the DB before running this test)
        val userId = 3 // Replace with a valid user ID

        val getUserResponse = webTestClient.get()
            .uri("/api/v1/users/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the response is successful
        Assertions.assertEquals(200, getUserResponse?.code)
        Assertions.assertEquals("User retrieved successfully.", getUserResponse?.message)
        Assertions.assertNotNull(getUserResponse?.data)

        // Check if the data property contains the user details
        val dataMapResponse = getUserResponse?.data as Map<*, *>
        Assertions.assertTrue(dataMapResponse.containsKey("id"))
        Assertions.assertTrue(dataMapResponse.containsKey("email"))
        Assertions.assertTrue(dataMapResponse.containsKey("role"))
        Assertions.assertTrue(dataMapResponse["id"] is Int)
        Assertions.assertTrue(dataMapResponse["email"] is String)
        Assertions.assertTrue(dataMapResponse["role"] is String)
    }

    @Test
    fun deleteUser () {
        // First, login to get a JWT token from a user with SUPER or ADMIN role
        val superUserCredentials = UserDTO("test@email.com", "Pass123#")

        val loginResponse = webTestClient.post()
            .uri("/api/v1/users/login")
            .bodyValue(superUserCredentials)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Extract the JWT token
        val dataMap = loginResponse?.data as Map<*, *>
        val token = dataMap["token"] as String

        // Now use the token to delete a user by ID (make sure the user exists in the DB before running this test)
        val userId = 6 // Replace with a valid user ID

        val deleteUserResponse = webTestClient.delete()
            .uri("/api/v1/users/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the response is successful
        Assertions.assertEquals(200, deleteUserResponse?.code)
        Assertions.assertEquals("User deleted successfully.", deleteUserResponse?.message)
        Assertions.assertNull(deleteUserResponse?.data)
    }

    @Test
    fun updateUser () {
        // First, login to get a JWT token from a user
        val superUserCredentials = UserDTO("test2@email.com", "Pass123#")

        val loginResponse = webTestClient.post()
            .uri("/api/v1/users/login")
            .bodyValue(superUserCredentials)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Extract the JWT token
        val dataMap = loginResponse?.data as Map<*, *>
        val token = dataMap["token"] as String

        // Now use the token to update a user by ID (make sure the user exists in the DB before running this test)
        val userId = 5 // Replace with a valid user ID

        // Create an instance of UpdateUserDTO with the new values
        val updateUserDTO = UpdateUserDTO(city = "Monterrey")

        val updateUserResponse = webTestClient.put()
            .uri("/api/v1/users/$userId")
            .header("Authorization", "Bearer $token")
            .bodyValue(updateUserDTO)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the response is successful
        Assertions.assertEquals(200, updateUserResponse?.code)
        Assertions.assertEquals("User details updated successfully.", updateUserResponse?.message)
    }
}
package com.jgmedellin.concept_coffee_shop.controller

import com.jgmedellin.concept_coffee_shop.CoffeeShopApplication
import com.jgmedellin.concept_coffee_shop.constants.UserRoles
import com.jgmedellin.concept_coffee_shop.dto.NewRoleDTO
import com.jgmedellin.concept_coffee_shop.dto.UpdateUserDTO
import com.jgmedellin.concept_coffee_shop.dto.UserDTO
import com.jgmedellin.concept_coffee_shop.entity.User
import com.jgmedellin.concept_coffee_shop.repository.UserRepository
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import com.jgmedellin.concept_coffee_shop.util.PostgreSQLContainerInitializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.collections.get

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [CoffeeShopApplication::class]
)
@ActiveProfiles("dev")
@AutoConfigureWebTestClient
class UserControllerIntegrationTests : PostgreSQLContainerInitializer() {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var userRepository: UserRepository

    private val passwordEncoder = BCryptPasswordEncoder()

    // Test user credentials
    private val testAdminEmail = "testadmin@example.com"
    private val testUserEmail = "testuser@example.com"
    private val testPassword = "TestPass123#"

    // JWT token for authenticated requests
    private var adminToken: String? = null
    private var userToken: String? = null

    // Test user IDs for cleanup and reference
    private var adminUserId: Int? = null
    private var regularUserId: Int? = null
    private var tempUserId: Int? = null

    @BeforeEach
    fun setup() {
        // Clean any existing test users
        userRepository.findByEmail(testAdminEmail)?.let { userRepository.delete(it) }
        userRepository.findByEmail(testUserEmail)?.let { userRepository.delete(it) }

        // Create test admin user
        val adminUser = User(
            email = testAdminEmail,
            password = passwordEncoder.encode(testPassword),
            role = UserRoles.SUPER
        )
        val savedAdminUser = userRepository.save(adminUser)
        adminUserId = savedAdminUser.id

        // Create test regular user
        val regularUser = User(
            email = testUserEmail,
            password = passwordEncoder.encode(testPassword),
            role = UserRoles.USER
        )
        val savedRegularUser = userRepository.save(regularUser)
        regularUserId = savedRegularUser.id

        // Login and get tokens
        adminToken = loginUser(testAdminEmail, testPassword)
        userToken = loginUser(testUserEmail, testPassword)
    }

    @AfterEach
    fun cleanup() {
        adminUserId?.let { userRepository.deleteById(it) }
        regularUserId?.let { userRepository.deleteById(it) }
        tempUserId?.let { userRepository.deleteById(it) }
        userRepository.deleteByEmail("test2@example.com")
    }

    /** Helper method to login and retrieve JWT token */
    private fun loginUser(email: String, password: String): String {
        val userCredentials = UserDTO(email, password)
        val loginResponse = webTestClient.post()
            .uri("/api/v1/users/login")
            .bodyValue(userCredentials)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        val dataMap = loginResponse?.data as Map<*, *>
        return dataMap["token"] as String
    }

    @Test
    @Order(1)
    fun registerUserTest() {
        val uniqueEmail = "test2@example.com"
        val userDTO = UserDTO(uniqueEmail, testPassword)

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

        // Verify user exists in DB
        val user = userRepository.findByEmail(uniqueEmail)
        Assertions.assertNotNull(user)
        Assertions.assertEquals(uniqueEmail, user?.email)
        Assertions.assertEquals(UserRoles.USER, user?.role)

        // Store ID for cleanup
        tempUserId = user?.id
    }

    @Test
    @Order(2)
    fun loginTest() {
        val userCredentials = UserDTO(testAdminEmail, testPassword)

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
    @Order(3)
    fun changeRoleTest() {
        Assertions.assertNotNull(adminToken)

        // Use admin token to change role of regular user
        val newRoleDTO = NewRoleDTO(testUserEmail, UserRoles.ADMIN)

        val changeRoleResponse = webTestClient.patch()
            .uri("/api/v1/users/changeRole")
            .header("Authorization", "Bearer $adminToken")
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

        // Verify DB was updated
        val updatedUser = userRepository.findByEmail(testUserEmail)
        Assertions.assertEquals(UserRoles.ADMIN, updatedUser?.role)
    }

    @Test
    @Order(4)
    fun getAllUsersTest() {
        Assertions.assertNotNull(adminToken, "Admin token should be available")

        // Use admin token to get all users
        val allUsersResponse = webTestClient.get()
            .uri("/api/v1/users")
            .header("Authorization", "Bearer $adminToken")
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

        // Verify our test users are in the response
        val foundEmails = dataMapResponse.map { (it as Map<*, *>)["email"] }.toList()
        Assertions.assertTrue(foundEmails.contains(testAdminEmail))
        Assertions.assertTrue(foundEmails.contains(testUserEmail))

        // Validate user properties
        dataMapResponse.forEach { user ->
            val userMap = user as Map<*, *>
            Assertions.assertTrue(userMap.containsKey("email"))
            Assertions.assertTrue(userMap.containsKey("role"))
            Assertions.assertTrue(userMap["email"] is String)
            Assertions.assertTrue(userMap["role"] is String)
        }
    }

    @Test
    @Order(5)
    fun getUserByIdTest() {
        Assertions.assertNotNull(adminToken)
        Assertions.assertNotNull(regularUserId)

        val getUserResponse = webTestClient.get()
            .uri("/api/v1/users/$regularUserId")
            .header("Authorization", "Bearer $adminToken")
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
        Assertions.assertEquals(regularUserId, dataMapResponse["id"])
        Assertions.assertEquals(testUserEmail, dataMapResponse["email"])
        Assertions.assertEquals(UserRoles.USER.toString(), dataMapResponse["role"])
    }

    @Test
    @Order(6)
    fun updateUserTest() {
        Assertions.assertNotNull(userToken)
        Assertions.assertNotNull(regularUserId)

        // Create an instance of UpdateUserDTO with the new values
        val updateUserDTO = UpdateUserDTO(username = "TestUserName", city = "Monterrey",
            phone = "1234567890", address = "123 Test St"
        )

        val updateUserResponse = webTestClient.put()
            .uri("/api/v1/users/$regularUserId")
            .header("Authorization", "Bearer $userToken")
            .bodyValue(updateUserDTO)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the response is successful
        Assertions.assertEquals(200, updateUserResponse?.code)
        Assertions.assertEquals("User details updated successfully.", updateUserResponse?.message)

        // Verify the user was updated in the database
        val updatedUser = userRepository.findById(regularUserId!!).orElse(null)
        Assertions.assertNotNull(updatedUser)
        Assertions.assertEquals("TestUserName", updatedUser.username)
        Assertions.assertEquals("Monterrey", updatedUser.city)
        Assertions.assertEquals("1234567890", updatedUser.phone)
        Assertions.assertEquals("123 Test St", updatedUser.address)
    }

    @Test
    @Order(7)
    fun deleteUserTest() {
        // Create a temporary user to delete
        val tempEmail = "tempdelete${System.currentTimeMillis()}@example.com"
        val tempUser = User(email = tempEmail, password = passwordEncoder.encode(testPassword),
            role = UserRoles.USER)
        val savedTempUser = userRepository.save(tempUser)
        val tempId = savedTempUser.id

        Assertions.assertNotNull(adminToken)
        Assertions.assertNotNull(tempId)

        val deleteUserResponse = webTestClient.delete()
            .uri("/api/v1/users/$tempId")
            .header("Authorization", "Bearer $adminToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the response is successful
        Assertions.assertEquals(200, deleteUserResponse?.code)
        Assertions.assertEquals("User deleted successfully.", deleteUserResponse?.message)
        Assertions.assertNull(deleteUserResponse?.data)

        // Verify the user was deleted from the database
        val deletedUser = userRepository.findById(tempId!!).orElse(null)
        Assertions.assertNull(deletedUser)
    }

    @Test
    @Order(8)
    fun forbiddenAccessTest() {
        Assertions.assertNotNull(userToken)
        Assertions.assertNotNull(adminUserId)

        // Regular user should not be able to update admin user
        val updateUserDTO = UpdateUserDTO(city = "Unauthorized City")

        webTestClient.put()
            .uri("/api/v1/users/$adminUserId")
            .header("Authorization", "Bearer $userToken")
            .bodyValue(updateUserDTO)
            .exchange()
            .expectStatus().isForbidden
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody
            .let {
                Assertions.assertEquals(403, it?.code)
                Assertions.assertEquals("You are not allowed to update other users.", it?.message)
            }
    }

    @Test
    @Order(9)
    fun invalidLoginTest() {
        val invalidCredentials = UserDTO(testAdminEmail, "WrongPassword123#")

        webTestClient.post()
            .uri("/api/v1/users/login")
            .bodyValue(invalidCredentials)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody
            .let {
                Assertions.assertEquals(400, it?.code)
                Assertions.assertEquals("Invalid credentials.", it?.message)
            }
    }
}
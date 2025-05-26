package com.jgmedellin.concept_coffee_shop.controller

import com.jgmedellin.concept_coffee_shop.CoffeeShopApplication
import com.jgmedellin.concept_coffee_shop.constants.ProductCategories
import com.jgmedellin.concept_coffee_shop.constants.UserRoles
import com.jgmedellin.concept_coffee_shop.dto.OrderDTO
import com.jgmedellin.concept_coffee_shop.dto.UserDTO
import com.jgmedellin.concept_coffee_shop.entity.Product
import com.jgmedellin.concept_coffee_shop.entity.User
import com.jgmedellin.concept_coffee_shop.repository.OrderRepository
import com.jgmedellin.concept_coffee_shop.repository.ProductRepository
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
class OrderControllerIntegrationTests : PostgreSQLContainerInitializer() {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var orderRepository: OrderRepository

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

        // Clean up products from previous test runs
        productRepository.deleteAll()

        // Clean up orders from previous test runs
        orderRepository.deleteAll()

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

        // Add products to the database for testing
        val products: List<Product> = listOf(
            Product(name = "Espresso", price = 2.5, images = listOf("espresso.jpg"),
                category = ProductCategories.DRINK, description = "Strong and bold coffee"),
            Product(name = "Donut", price = 2.0, images = listOf("donut.jpg"),
                category = ProductCategories.FOOD, description = "Creamy and smooth coffee")
        )
        productRepository.saveAll(products)

        // Create an order for the regular user
        val orderDTO = OrderDTO(customerEmail = testUserEmail, orderItems = listOf("0", "1"), totalAmount = 4.5)

        webTestClient.post()
            .uri("/api/v1/orders")
            .header("Authorization", "Bearer $userToken")
            .bodyValue(orderDTO)
            .exchange()
            .expectStatus().isCreated
    }

    @AfterEach
    fun cleanup() {
        // Clean up users
        adminUserId?.let { userRepository.deleteById(it) }
        regularUserId?.let { userRepository.deleteById(it) }
        tempUserId?.let { userRepository.deleteById(it) }
        userRepository.deleteByEmail("test2@example.com")

        // Clean up products
        productRepository.deleteAll()

        // Clean up orders
        orderRepository.deleteAll()
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
    fun createOrderTest() {
        // Create a sample order DTO
        val orderDTO = OrderDTO(customerEmail = testUserEmail, orderItems = listOf("0", "1"), totalAmount = 4.5)

        // Create the order
        val response = webTestClient.post()
            .uri("/api/v1/orders")
            .header("Authorization", "Bearer $userToken")
            .bodyValue(orderDTO)
            .exchange()
            .expectStatus().isCreated
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Validate the response
        Assertions.assertNotNull(response)
        Assertions.assertEquals("Order created successfully", response?.message)
        Assertions.assertNotNull(response?.data)
        val dataMap = response?.data as Map<*, *>
        Assertions.assertTrue(dataMap.containsKey("orderId"))
        val orderId = dataMap["orderId"] as String
        Assertions.assertTrue(orderId.isNotEmpty())
    }

    @Test
    @Order(2)
    fun adminCanNotCreateOrderTest() {
        // Create a sample order DTO
        val orderDTO = OrderDTO(customerEmail = testUserEmail, orderItems = listOf("0", "1"), totalAmount = 4.5)

        // Attempt to create the order as an admin user
        webTestClient.post()
            .uri("/api/v1/orders")
            .header("Authorization", "Bearer $adminToken")
            .bodyValue(orderDTO)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    @Order(3)
    fun getMyOrdersTest() {
        // Get orders for the logged-in user
        val response = webTestClient.get()
            .uri("/api/v1/orders/history")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Validate the response
        Assertions.assertNotNull(response)
        Assertions.assertEquals("Orders retrieved successfully", response?.message)
        Assertions.assertNotNull(response?.data)
        val dataMap = response?.data as Map<*, *>
        val ordersList = dataMap["orders"] as List<*>
        Assertions.assertTrue(ordersList.isNotEmpty())
    }

    @Test
    @Order(4)
    fun getAllOrdersTest() {
        // Get all orders as an admin user
        val response = webTestClient.get()
            .uri("/api/v1/orders")
            .header("Authorization", "Bearer $adminToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Validate the response
        Assertions.assertNotNull(response)
        Assertions.assertEquals("Orders retrieved successfully", response?.message)
        Assertions.assertNotNull(response?.data)
        val dataMap = response?.data as Map<*, *>
        val ordersList = dataMap["orders"] as List<*>
        Assertions.assertTrue(ordersList.isNotEmpty())
        Assertions.assertTrue(ordersList.any { (it as Map<*, *>)["customerEmail"] == testUserEmail })
    }

    @Test
    @Order(5)
    fun userCanNotGetAllOrdersTest() {
        // Attempt to get all orders as a regular user
        webTestClient.get()
            .uri("/api/v1/orders")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    @Order(6)
    fun getOneOrderTest() {
        // Get the first order for the logged-in user
        val response = webTestClient.get()
            .uri("/api/v1/orders/history")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Validate the response
        Assertions.assertNotNull(response)
        Assertions.assertEquals("Orders retrieved successfully", response?.message)
        Assertions.assertNotNull(response?.data)
        val dataMap = response?.data as Map<*, *>
        val ordersList = dataMap["orders"] as List<*>
        Assertions.assertTrue(ordersList.isNotEmpty())

        // Get the first order ID
        val firstOrderId = (ordersList[0] as Map<*, *>)["orderId"] as String

        // Get the specific order by ID
        val orderResponse = webTestClient.get()
            .uri("/api/v1/orders/$firstOrderId")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Validate the order response
        Assertions.assertNotNull(orderResponse)
        Assertions.assertEquals("Order retrieved successfully", orderResponse?.message)
        Assertions.assertNotNull(orderResponse?.data)
    }

    @Test
    @Order(7)
    fun deleteOrderTest() {
        // Get the first order for the logged-in user
        val response = webTestClient.get()
            .uri("/api/v1/orders/history")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Validate the response
        Assertions.assertNotNull(response)
        Assertions.assertEquals("Orders retrieved successfully", response?.message)
        Assertions.assertNotNull(response?.data)
        val dataMap = response?.data as Map<*, *>
        val ordersList = dataMap["orders"] as List<*>
        Assertions.assertTrue(ordersList.isNotEmpty())

        // Get the first order ID
        val firstOrderId = (ordersList[0] as Map<*, *>)["orderId"] as String

        // Delete the specific order by ID
        webTestClient.delete()
            .uri("/api/v1/orders/$firstOrderId")
            .header("Authorization", "Bearer $adminToken")
            .exchange()
            .expectStatus().isNoContent

        // Verify the order is deleted
        webTestClient.get()
            .uri("/api/v1/orders/$firstOrderId")
            .header("Authorization", "Bearer $adminToken")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    @Order(8)
    fun userCanNotDeleteOrderTest() {
        // Get the first order for the logged-in user
        val response = webTestClient.get()
            .uri("/api/v1/orders/history")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Validate the response
        Assertions.assertNotNull(response)
        Assertions.assertEquals("Orders retrieved successfully", response?.message)
        Assertions.assertNotNull(response?.data)
        val dataMap = response?.data as Map<*, *>
        val ordersList = dataMap["orders"] as List<*>
        Assertions.assertTrue(ordersList.isNotEmpty())

        // Get the first order ID
        val firstOrderId = (ordersList[0] as Map<*, *>)["orderId"] as String

        // Attempt to delete the specific order by ID as a regular user
        webTestClient.delete()
            .uri("/api/v1/orders/$firstOrderId")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    @Order(9)
    fun updateOrderTest() {
        // Get the first order for the logged-in user
        val response = webTestClient.get()
            .uri("/api/v1/orders/history")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Validate the response
        Assertions.assertNotNull(response)
        Assertions.assertEquals("Orders retrieved successfully", response?.message)
        Assertions.assertNotNull(response?.data)
        val dataMap = response?.data as Map<*, *>
        val ordersList = dataMap["orders"] as List<*>
        Assertions.assertTrue(ordersList.isNotEmpty())

        // Get the first order ID
        val firstOrderId = (ordersList[0] as Map<*, *>)["orderId"] as String

        // Use a valid OrderStatus value (IN_PROGRESS instead of PROCESSING)
        val jsonBody = """{"orderStatus": "IN_PROGRESS"}""".trimIndent()

        webTestClient.patch()
            .uri("/api/v1/orders/$firstOrderId")
            .header("Authorization", "Bearer $adminToken")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(jsonBody)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @Order(10)
    fun userCanNotUpdateOrderTest() {
        // Get the first order for the logged-in user
        val response = webTestClient.get()
            .uri("/api/v1/orders/history")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Validate the response
        Assertions.assertNotNull(response)
        Assertions.assertEquals("Orders retrieved successfully", response?.message)
        Assertions.assertNotNull(response?.data)
        val dataMap = response?.data as Map<*, *>
        val ordersList = dataMap["orders"] as List<*>
        Assertions.assertTrue(ordersList.isNotEmpty())

        // Get the first order ID
        val firstOrderId = (ordersList[0] as Map<*, *>)["orderId"] as String

        // Attempt to update the specific order by ID as a regular user
        val jsonBody = """{"orderStatus": "IN_PROGRESS"}""".trimIndent()

        webTestClient.patch()
            .uri("/api/v1/orders/$firstOrderId")
            .header("Authorization", "Bearer $userToken")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(jsonBody)
            .exchange()
            .expectStatus().isForbidden
    }

}
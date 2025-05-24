package com.jgmedellin.concept_coffee_shop.controller

import com.jgmedellin.concept_coffee_shop.CoffeeShopApplication
import com.jgmedellin.concept_coffee_shop.constants.ProductCategories
import com.jgmedellin.concept_coffee_shop.constants.UserRoles
import com.jgmedellin.concept_coffee_shop.dto.NewRoleDTO
import com.jgmedellin.concept_coffee_shop.dto.ProductDTO
import com.jgmedellin.concept_coffee_shop.dto.UpdateUserDTO
import com.jgmedellin.concept_coffee_shop.dto.UserDTO
import com.jgmedellin.concept_coffee_shop.entity.Product
import com.jgmedellin.concept_coffee_shop.entity.User
import com.jgmedellin.concept_coffee_shop.repository.ProductRepository
import com.jgmedellin.concept_coffee_shop.repository.UserRepository
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
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
class ProductControllerIntegrationTests {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var productRepository: ProductRepository

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

        // Clean up products from previous test runs
        productRepository.deleteAll()

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
    fun createProductsTest() {
        // Create a list of product DTOs to test
        val productsDTOs = listOf(
            ProductDTO(name = "Espresso", price = 2.5, images = listOf("espresso.jpg"),
                category = ProductCategories.DRINK, description = "Strong and bold coffee"),
            ProductDTO(
                name = "Donut", price = 2.0, images = listOf("donut.jpg"), category = ProductCategories.FOOD,
                description = "Creamy and smooth coffee")
        )

        // Use admin token to create products
        val createResponse = webTestClient.post()
            .uri("/api/v1/products")
            .header("Authorization", "Bearer $adminToken")
            .bodyValue(productsDTOs)
            .exchange()
            .expectStatus().isCreated
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the response is successful
        Assertions.assertEquals(201, createResponse?.code)
        Assertions.assertEquals("Products created successfully", createResponse?.message)
        Assertions.assertNotNull(createResponse?.data)

        // Verify products were saved in the database
        val productsInDb = productRepository.findAll()
        Assertions.assertEquals(2, productsInDb.count())
    }

    @Test
    @Order(2)
    fun createProductWithoutAuthorizationTest() {
        // Create a list of product DTOs to test
        val productsDTOs = listOf(
            ProductDTO(name = "Espresso", price = 2.5, images = listOf("espresso.jpg"),
                category = ProductCategories.DRINK, description = "Strong and bold coffee"),
            ProductDTO(
                name = "Donut", price = 2.0, images = listOf("donut.jpg"), category = ProductCategories.FOOD,
                description = "Creamy and smooth coffee")
        )

        // Attempt to create products without authorization
        val createResponse = webTestClient.post()
            .uri("/api/v1/products")
            .bodyValue(productsDTOs)
            .exchange()
            .expectStatus().isForbidden
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

    }

    @Test
    @Order(3)
    fun getAllProductsTest() {
        // Use admin token to get all products
        val allProductsResponse = webTestClient.get()
            .uri("/api/v1/products")
            .header("Authorization", "Bearer $adminToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the response is successful
        Assertions.assertEquals(200, allProductsResponse?.code)
        Assertions.assertEquals("Products retrieved successfully", allProductsResponse?.message)
        Assertions.assertNotNull(allProductsResponse?.data)

        // Check if the data property contains a list of products
        val dataMapResponse = allProductsResponse?.data as List<*>
        Assertions.assertTrue(dataMapResponse.isNotEmpty())

        // Validate product properties
        dataMapResponse.forEach { product ->
            val productMap = product as Map<*, *>
            Assertions.assertTrue(productMap.containsKey("id"))
            Assertions.assertTrue(productMap.containsKey("name"))
            Assertions.assertTrue(productMap.containsKey("price"))
            Assertions.assertTrue(productMap["id"] is Int)
            Assertions.assertTrue(productMap["name"] is String)
            Assertions.assertTrue(productMap["price"] is Double)
        }
    }

    @Test
    @Order(4)
    fun getProductByIdTest() {
        // Get the ID of the first product in the database
        val productId = productRepository.findAll().first().id

        // Use admin token to get product by ID
        val getProductResponse = webTestClient.get()
            .uri("/api/v1/products/$productId")
            .header("Authorization", "Bearer $adminToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the response is successful
        Assertions.assertEquals(200, getProductResponse?.code)
        Assertions.assertEquals("Product retrieved successfully", getProductResponse?.message)
        Assertions.assertNotNull(getProductResponse?.data)

        // Check if the data property contains the product details
        val dataMapResponse = getProductResponse?.data as Map<*, *>
        Assertions.assertTrue(dataMapResponse.containsKey("name"))
        Assertions.assertTrue(dataMapResponse.containsKey("price"))
    }

    @Test
    @Order(5)
    fun deleteProductTest() {
        // Get the ID of the first product in the database
        val productId = productRepository.findAll().first().id

        // Use admin token to delete product by ID
        val deleteProductResponse = webTestClient.delete()
            .uri("/api/v1/products/$productId")
            .header("Authorization", "Bearer $adminToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the response is successful
        Assertions.assertEquals(200, deleteProductResponse?.code)
        Assertions.assertEquals("Product deleted successfully", deleteProductResponse?.message)

        // Verify product was deleted from the database
        val deletedProduct = productRepository.findById(productId?.toInt() ?: 1)
        Assertions.assertFalse(deletedProduct.isPresent)
    }

    @Test
    @Order(6)
    fun deleteProductWithoutAuthorizationTest() {
        // Get the ID of the first product in the database
        val productId = productRepository.findAll().first().id

        // Attempt to delete product without authorization
        val deleteProductResponse = webTestClient.delete()
            .uri("/api/v1/products/$productId")
            .exchange()
            .expectStatus().isForbidden
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody
    }

    @Test
    @Order(7)
    fun updateProductTest() {
        // Get the ID of the first product in the database
        val productId = productRepository.findAll().first().id

        // Create a product DTO with updated values
        val updatedProductDTO = ProductDTO(name = "Updated Espresso", price = 3.0,
            images = listOf("updated_espresso.jpg"), category = ProductCategories.DRINK,
            description = "Updated strong and bold coffee")

        // Use admin token to update product by ID
        val updateProductResponse = webTestClient.patch()
            .uri("/api/v1/products/$productId")
            .header("Authorization", "Bearer $adminToken")
            .bodyValue(updatedProductDTO)
            .exchange()
            .expectStatus().isOk
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody

        // Check if the response is successful
        Assertions.assertEquals(200, updateProductResponse?.code)
        Assertions.assertEquals("Product updated successfully", updateProductResponse?.message)

        // Verify product was updated in the database
        val updatedProduct = productRepository.findById(productId?.toInt() ?: 1)
        Assertions.assertTrue(updatedProduct.isPresent)
        Assertions.assertEquals("Updated strong and bold coffee", updatedProduct.get().description)
    }

    @Test
    @Order(8)
    fun updateProductWithoutAuthorizationTest() {
        // Get the ID of the first product in the database
        val productId = productRepository.findAll().first().id

        // Create a product DTO with updated values
        val updatedProductDTO = ProductDTO(name = "Updated Espresso", price = 3.0,
            images = listOf("updated_espresso.jpg"), category = ProductCategories.DRINK,
            description = "Updated strong and bold coffee")

        // Attempt to update product without authorization
        val updateProductResponse = webTestClient.patch()
            .uri("/api/v1/products/$productId")
            .bodyValue(updatedProductDTO)
            .exchange()
            .expectStatus().isForbidden
            .expectBody(GeneralResponse::class.java)
            .returnResult()
            .responseBody
    }

}
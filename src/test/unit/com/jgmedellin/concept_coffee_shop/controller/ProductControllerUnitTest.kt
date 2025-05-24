package com.jgmedellin.concept_coffee_shop.controller

import com.jgmedellin.concept_coffee_shop.constants.ProductCategories
import com.jgmedellin.concept_coffee_shop.constants.UserRoles
import com.jgmedellin.concept_coffee_shop.dto.NewRoleDTO
import com.jgmedellin.concept_coffee_shop.dto.ProductDTO
import com.jgmedellin.concept_coffee_shop.dto.UpdateUserDTO
import com.jgmedellin.concept_coffee_shop.dto.UserDTO
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import com.jgmedellin.concept_coffee_shop.service.ProductService
import com.jgmedellin.concept_coffee_shop.service.UserService
import com.jgmedellin.concept_coffee_shop.util.JwtUtil
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.Assert
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.junit5.JUnit5Asserter.assertNotNull

@WebMvcTest(controllers = [ProductController::class])
@AutoConfigureWebTestClient
class ProductControllerUnitTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @MockkBean
    lateinit var userServiceMock: UserService

    @MockkBean
    lateinit var productServiceMock: ProductService

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
    fun createProductsTest() {
        val productDTOs = listOf(
            ProductDTO(1,"Product 1", "Description 1", 10.0,
                ProductCategories.DRINK, listOf("image1.jpg")),
            ProductDTO(2, "Product 2", "Description 2", 20.0,
                ProductCategories.FOOD, listOf("image2.jpg"))
        )

        val responseEntity = ResponseEntity(
            GeneralResponse(201, "Products created successfully", productDTOs),
            HttpStatus.CREATED
        )

        every { productServiceMock.createProducts(productDTOs) } returns responseEntity

        webTestClient.post()
            .uri("/api/v1/products")
            .bodyValue(productDTOs)
            .exchange()
            .expectStatus().isCreated
            .expectBody()

        Assert.assertEquals(HttpStatus.CREATED, responseEntity.statusCode)
        Assert.assertEquals("Products created successfully", responseEntity.body?.message)
        Assert.assertEquals(productDTOs, responseEntity.body?.data)
    }

    @Test
    fun createProduct_validation() {
        // Create a list with an invalid product
        val productsDTOs = listOf(
            ProductDTO(1, "", "", 0.0,
                ProductCategories.DRINK, listOf("image1.jpg"))
        )

        // Use expectBodyList to catch the error response
        webTestClient.post()
            .uri("/api/v1/products")
            .bodyValue(productsDTOs)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(String::class.java)
            .returnResult()
            .responseBody?.let { responseBody ->
                // Assert that the response contains validation error messages
                assertTrue(responseBody.contains("Product name cannot be blank"))
                assertTrue(responseBody.contains("Product description cannot be blank"))
                assertTrue(responseBody.contains("Price must be greater than 0.0"))
            }
    }

    @Test
    fun getAllProductsTest() {
        val productDTOs = listOf(
            ProductDTO(1,"Product 1", "Description 1", 10.0,
                ProductCategories.DRINK, listOf("image1.jpg")),
            ProductDTO(2, "Product 2", "Description 2", 20.0,
                ProductCategories.FOOD, listOf("image2.jpg"))
        )

        val responseEntity = ResponseEntity(
            GeneralResponse(200, "Products retrieved successfully", productDTOs),
            HttpStatus.OK
        )

        every { productServiceMock.getAllProducts() } returns responseEntity

        webTestClient.get()
            .uri("/api/v1/products")
            .exchange()
            .expectStatus().isOk
            .expectBody()

        Assert.assertEquals(HttpStatus.OK, responseEntity.statusCode)
        Assert.assertEquals("Products retrieved successfully", responseEntity.body?.message)
        Assert.assertEquals(productDTOs, responseEntity.body?.data)
    }

    @Test
    fun getProductByIdTest() {
        val productDTO = ProductDTO(1,"Product 1", "Description 1", 10.0,
            ProductCategories.DRINK, listOf("image1.jpg"))

        val responseEntity = ResponseEntity(
            GeneralResponse(200, "Product retrieved successfully", productDTO),
            HttpStatus.OK
        )

        every { productServiceMock.getProductById(1) } returns responseEntity

        webTestClient.get()
            .uri("/api/v1/products/1")
            .exchange()
            .expectStatus().isOk
            .expectBody()

        Assert.assertEquals(HttpStatus.OK, responseEntity.statusCode)
        Assert.assertEquals("Product retrieved successfully", responseEntity.body?.message)
        Assert.assertEquals(productDTO, responseEntity.body?.data)
    }

    @Test
    fun deleteProductTest() {
        val responseEntity = ResponseEntity(
            GeneralResponse(200, "Product deleted successfully"),
            HttpStatus.OK
        )

        every { productServiceMock.deleteProduct(1) } returns responseEntity

        webTestClient.delete()
            .uri("/api/v1/products/1")
            .exchange()
            .expectStatus().isOk
            .expectBody()

        Assert.assertEquals(HttpStatus.OK, responseEntity.statusCode)
        Assert.assertEquals("Product deleted successfully", responseEntity.body?.message)
    }

    @Test
    fun updateProductTest() {
        val productDTO = ProductDTO(1,"Product 1", "Description 1", 10.0,
            ProductCategories.DRINK, listOf("image1.jpg"))

        val responseEntity = ResponseEntity(
            GeneralResponse(200, "Product updated successfully", productDTO),
            HttpStatus.OK
        )

        every { productServiceMock.updateProduct(1, productDTO) } returns responseEntity

        webTestClient.patch()
            .uri("/api/v1/products/1")
            .bodyValue(productDTO)
            .exchange()
            .expectStatus().isOk
            .expectBody()

        Assert.assertEquals(HttpStatus.OK, responseEntity.statusCode)
        Assert.assertEquals("Product updated successfully", responseEntity.body?.message)
        Assert.assertEquals(productDTO, responseEntity.body?.data)
    }

    @Test
    fun getProductById_notFound() {
        val responseEntity = ResponseEntity(
            GeneralResponse(404, "Product not found"),
            HttpStatus.NOT_FOUND
        )

        every { productServiceMock.getProductById(999) } returns responseEntity

        webTestClient.get()
            .uri("/api/v1/products/999")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()

        Assert.assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)
        Assert.assertEquals("Product not found", responseEntity.body?.message)
    }
}
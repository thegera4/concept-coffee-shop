package com.jgmedellin.concept_coffee_shop.controller

import com.jgmedellin.concept_coffee_shop.constants.OrderStatus
import com.jgmedellin.concept_coffee_shop.dto.OrderDTO
import com.jgmedellin.concept_coffee_shop.dto.UpdateOrderDTO
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import com.jgmedellin.concept_coffee_shop.service.OrderService
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

@WebMvcTest(controllers = [OrderController::class])
@AutoConfigureWebTestClient
class OrderControllerUnitTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @MockkBean
    lateinit var userServiceMock: UserService

    @MockkBean
    lateinit var productServiceMock: ProductService

    @MockkBean
    lateinit var orderServiceMock: OrderService

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
    fun createOrderTest() {
        val orderDTO = OrderDTO(customerEmail = "test2@email.com", orderItems =  listOf("0", "1"), totalAmount = 4.5)

        val responseEntity = ResponseEntity(
            GeneralResponse(201, "Order created successfully", orderDTO),
            HttpStatus.CREATED
        )

        every { orderServiceMock.createOrder(orderDTO) } returns responseEntity

        webTestClient.post()
            .uri("/api/v1/orders")
            .bodyValue(orderDTO)
            .exchange()
            .expectStatus().isCreated
            .expectBody()

        Assert.assertEquals(HttpStatus.CREATED, responseEntity.statusCode)
        Assert.assertEquals("Order created successfully", responseEntity.body?.message)
        Assert.assertEquals(orderDTO, responseEntity.body?.data)
    }

    @Test
    fun createOrder_validation() {
        // Create an invalid order with empty customerEmail and orderItems
        val orderDTO = OrderDTO(customerEmail = "", orderItems = emptyList(), totalAmount = 0.0)

        // Use expectBody to catch the error response
        webTestClient.post()
            .uri("/api/v1/orders")
            .bodyValue(orderDTO)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun getMyOrdersTest() {
        val orderDTOs = listOf(
            OrderDTO(customerEmail = "test2@email.com", orderItems = listOf("0", "1"), totalAmount = 4.5),
            OrderDTO(customerEmail = "test2@email.com", orderItems = listOf("2", "3"), totalAmount = 9.0)
        )
        val responseEntity = ResponseEntity(
            GeneralResponse(200, "Orders retrieved successfully", orderDTOs),
            HttpStatus.OK
        )

        every { orderServiceMock.getMyOrders() } returns responseEntity

        webTestClient.get()
            .uri("/api/v1/orders/history")
            .exchange()
            .expectStatus().isOk
            .expectBody()

        Assert.assertEquals(HttpStatus.OK, responseEntity.statusCode)
        Assert.assertEquals("Orders retrieved successfully", responseEntity.body?.message)
        Assert.assertEquals(orderDTOs, responseEntity.body?.data)
    }

    @Test
    fun getAllOrdersTest() {
        val orderDTOs = listOf(
            OrderDTO(customerEmail = "test2@email.com", orderItems = listOf("0", "1"), totalAmount = 4.5),
            OrderDTO(customerEmail = "test2@email.com", orderItems = listOf("2", "3"), totalAmount = 9.0)
        )

        val responseEntity = ResponseEntity(
            GeneralResponse(200, "Orders retrieved successfully", orderDTOs),
            HttpStatus.OK
        )

        every { orderServiceMock.getAllOrders(10, null) } returns responseEntity

        webTestClient.get()
            .uri("/api/v1/orders?size=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()

        Assert.assertEquals(HttpStatus.OK, responseEntity.statusCode)
        Assert.assertEquals("Orders retrieved successfully", responseEntity.body?.message)
        Assert.assertEquals(orderDTOs, responseEntity.body?.data)
    }

    @Test
    fun getOrderByIdTest() {
        val orderDTO = OrderDTO(customerEmail = "test2@email.com", orderItems = listOf("0", "1"), totalAmount = 4.5)

        val responseEntity = ResponseEntity(
            GeneralResponse(200, "Order retrieved successfully", orderDTO),
            HttpStatus.OK
        )

        every { orderServiceMock.getOrder("1") } returns responseEntity

        webTestClient.get()
            .uri("/api/v1/orders/1")
            .exchange()
            .expectStatus().isOk
            .expectBody()

        Assert.assertEquals(HttpStatus.OK, responseEntity.statusCode)
        Assert.assertEquals("Order retrieved successfully", responseEntity.body?.message)
        Assert.assertEquals(orderDTO, responseEntity.body?.data)
    }

    @Test
    fun deleteOrderTest() {
        val responseEntity = ResponseEntity(
            GeneralResponse(200, "Order deleted successfully"),
            HttpStatus.OK
        )

        every { orderServiceMock.deleteOrder("1") } returns responseEntity

        webTestClient.delete()
            .uri("/api/v1/orders/1")
            .exchange()
            .expectStatus().isOk
            .expectBody()

        Assert.assertEquals(HttpStatus.OK, responseEntity.statusCode)
        Assert.assertEquals("Order deleted successfully", responseEntity.body?.message)
    }

    @Test
    fun updateOrderTest() {
        val updateOrderDTO = UpdateOrderDTO(orderStatus = OrderStatus.IN_PROGRESS)

        val responseEntity = ResponseEntity(
            GeneralResponse(200, "Order updated successfully"),
            HttpStatus.OK
        )

        every { orderServiceMock.updateOrder("1", updateOrderDTO) } returns responseEntity

        webTestClient.patch()
            .uri("/api/v1/orders/1")
            .bodyValue(updateOrderDTO)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()
            .responseBody

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        assertEquals("Order updated successfully", responseEntity.body?.message)
        assertTrue(responseEntity.body?.data == null, "Data should be null for update response")
        assertEquals(null, responseEntity.body?.data, "Data should be null for update response")
    }
}
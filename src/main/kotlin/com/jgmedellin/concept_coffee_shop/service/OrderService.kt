package com.jgmedellin.concept_coffee_shop.service

import com.jgmedellin.concept_coffee_shop.dto.OrderDTO
import com.jgmedellin.concept_coffee_shop.entity.Order
import com.jgmedellin.concept_coffee_shop.repository.OrderRepository
import com.jgmedellin.concept_coffee_shop.repository.UserRepository
import com.jgmedellin.concept_coffee_shop.repository.ProductRepository
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class OrderService(val orderRepository: OrderRepository, val userRepository: UserRepository,
                   val productRepository: ProductRepository) {
    /**
     * Order service method to create a new order.
     * @param orderDTO The order data transfer object containing order details.
     * @return ResponseEntity with the created order.
     */
    fun createOrder(orderDTO: OrderDTO): ResponseEntity<GeneralResponse> {
        // Get the user by email
        val user = userRepository.findByEmail(orderDTO.customerEmail)
            ?: return ResponseEntity(
                GeneralResponse(400, "User with email ${orderDTO.customerEmail} not found"),
                HttpStatus.BAD_REQUEST
            )
        // Convert product IDs to actual Product entities
        val products = orderDTO.orderItems.mapNotNull { productId ->
            try {
                productRepository.findById(productId.toInt()).orElse(null)
            } catch (e: NumberFormatException) {
                null
            }
        }

        if (products.isEmpty()) {
            return ResponseEntity(
                GeneralResponse(400, "No valid products found for the given IDs"),
                HttpStatus.BAD_REQUEST
            )
        }

        // Create order with product entities
        val order = Order(user = user, products = products.toMutableList(), totalAmount = orderDTO.totalAmount)

        return try {
            val createdOrder = orderRepository.save(order)
            ResponseEntity(
                GeneralResponse(201, "Order created successfully", mapOf(
                    "orderId" to createdOrder.id, "customerEmail" to createdOrder.user.email,
                    "products" to createdOrder.products.map { it.name }, "totalAmount" to createdOrder.totalAmount
                )),
                HttpStatus.CREATED
            )
        } catch (e: Exception) {
            ResponseEntity(
                GeneralResponse(500, "Error creating order: ${e.message}"),
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }

    }

}
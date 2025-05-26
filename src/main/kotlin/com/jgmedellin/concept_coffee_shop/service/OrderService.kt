package com.jgmedellin.concept_coffee_shop.service

import com.jgmedellin.concept_coffee_shop.constants.UserRoles
import com.jgmedellin.concept_coffee_shop.dto.OrderDTO
import com.jgmedellin.concept_coffee_shop.dto.UpdateOrderDTO
import com.jgmedellin.concept_coffee_shop.entity.Order
import com.jgmedellin.concept_coffee_shop.repository.OrderRepository
import com.jgmedellin.concept_coffee_shop.repository.UserRepository
import com.jgmedellin.concept_coffee_shop.repository.ProductRepository
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class OrderService(
    val orderRepository: OrderRepository,
    val userRepository: UserRepository,
    val productRepository: ProductRepository
) {
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

    /**
     * Order service method to retrieve all orders for the authenticated user.
     * @return ResponseEntity with the list of orders.
     */
    fun getMyOrders(): ResponseEntity<GeneralResponse> {
        // Get the authenticated user's email from the security context
        val userEmail = "${SecurityContextHolder.getContext().authentication.name}"

        // Find the user by email
        userRepository.findByEmail(userEmail) ?:
            return ResponseEntity(
                GeneralResponse(404, "User with email $userEmail not found"),
                HttpStatus.NOT_FOUND
            )

        // Retrieve orders for the user
        val orders = orderRepository.findAllByUserEmail(userEmail)

        // Check if orders are found
        if (orders.isEmpty()) {
            return ResponseEntity(
                GeneralResponse(404, "No orders found for user with email $userEmail"),
                HttpStatus.NOT_FOUND
            )
        }

        // Map orders to a response format
        val orderList = orders.map { order -> mapOf("orderId" to order.id) }

        return ResponseEntity(
            GeneralResponse(200, "Orders retrieved successfully",
                mapOf("orders" to orderList)),
            HttpStatus.OK
        )
    }

    /**
     * Order service method to retrieve all orders with optional filtering by email.
     * @param size The number of orders to retrieve.
     * @param email Optional email parameter to filter orders by customer email.
     * @return ResponseEntity with the list of orders.
     */
    fun getAllOrders(size: Int, email: String?): ResponseEntity<GeneralResponse> {
        // Check if email is provided for filtering
        val orders = if(email != null) orderRepository.findAllByUserEmail(email) else orderRepository.findAll().toList()

        // Check if orders are found
        if (orders.isEmpty()) return ResponseEntity(
            GeneralResponse(404, "No orders found"), HttpStatus.NOT_FOUND)

        // Limit the number of orders returned based on the size parameter
        val limitedOrders = if (size > 0) orders.take(size) else orders
        if (limitedOrders.isEmpty()) return ResponseEntity(
            GeneralResponse(404, "No orders found after applying size limit"),
            HttpStatus.NOT_FOUND)

        // Map orders to a response format
        val orderList = limitedOrders.map { order -> mapOf("orderId" to order.id, "customerEmail" to order.user.email) }

        return ResponseEntity(
            GeneralResponse(200, "Orders retrieved successfully",
                mapOf("orders" to orderList)),
            HttpStatus.OK
        )
    }

    /**
     * Order service method to retrieve a single order by its ID.
     * @param id The ID of the order to be retrieved.
     * @return ResponseEntity with the order details.
     */
    fun getOrder(id: String): ResponseEntity<GeneralResponse> {
        // Check if the current user is authenticated
        val userEmail = "${SecurityContextHolder.getContext().authentication.name}"
        val user = userRepository.findByEmail(userEmail)
            ?: return ResponseEntity(
                GeneralResponse(404, "User with email $userEmail not found"),
                HttpStatus.NOT_FOUND
            )
        val isAdmin = user.role == UserRoles.ADMIN || user.role == UserRoles.SUPER
        // If the user is not an admin or super, check if the order belongs to the user
        if (!isAdmin) {
            val order = orderRepository.findById(id).orElse(null)
            if (order == null || order.user.email != userEmail) {
                return ResponseEntity(
                    GeneralResponse(403, "You do not have permission to access this order"),
                    HttpStatus.FORBIDDEN
                )
            }
        }
        // If the user is an admin or super, they can access any order
        val order = orderRepository.findById(id).orElse(null)
        if (order == null) {
            return ResponseEntity(
                GeneralResponse(404, "Order with ID $id not found"),
                HttpStatus.NOT_FOUND
            )
        }
        // Map order to a response format
        val orderDetails = mapOf("orderId" to order.id, "customerEmail" to order.user.email,
            "products" to order.products.map { it.name }, "totalAmount" to order.totalAmount,
            "status" to order.status.name
        )

        return ResponseEntity(
            GeneralResponse(200, "Order retrieved successfully", orderDetails),
            HttpStatus.OK
        )
    }

    /**
     * Order service method to delete an order by its ID.
     * @param id The ID of the order to be deleted.
     * @return ResponseEntity indicating the result of the deletion operation.
     */
    fun deleteOrder(id: String): ResponseEntity<GeneralResponse> {
        // Check if the order exists
        val order = orderRepository.findById(id).orElse(null)
        if (order == null) {
            return ResponseEntity(
                GeneralResponse(404, "Order with ID $id not found"),
                HttpStatus.NOT_FOUND
            )
        }

        // Delete the order
        orderRepository.delete(order)

        return ResponseEntity(
            GeneralResponse(204, "Order deleted successfully"), HttpStatus.NO_CONTENT
        )
    }

    /**
     * Order service method to update an order by its ID.
     * @param id The ID of the order to be updated.
     * @param updateOrderDTO The order data transfer object containing updated order details.
     * @return ResponseEntity with the updated order.
     */
    fun updateOrder(id: String, updateOrderDTO: UpdateOrderDTO): ResponseEntity<GeneralResponse> {
        // Check if the order exists
        val order = orderRepository.findById(id).orElse(null)
        if (order == null) {
            return ResponseEntity(
                GeneralResponse(404, "Order with ID $id not found"),
                HttpStatus.NOT_FOUND
            )
        }

        // Update order details
        order.status = updateOrderDTO.orderStatus
        // If orderItems are provided, update the products in the order
        if (updateOrderDTO.orderItems != null) {
            val products = updateOrderDTO.orderItems?.mapNotNull { productId ->
                try {
                    productRepository.findById(productId.toInt()).orElse(null)
                } catch (e: NumberFormatException) {
                    null
                }
            }?.toMutableList()

            if (products?.isEmpty() == true) {
                return ResponseEntity(
                    GeneralResponse(400, "No valid products found for the given IDs"),
                    HttpStatus.BAD_REQUEST
                )
            }
            order.products = products ?: mutableListOf()
        } else {
            order.products = order.products // Keep existing products if no new ones are provided
        }

        // Save the updated order
        return try {
            val updatedOrder = orderRepository.save(order)
            ResponseEntity(
                GeneralResponse(200, "Order updated successfully", mapOf(
                    "products" to updatedOrder.products.map { it.name }, "status" to updatedOrder.status.name)),
                HttpStatus.OK
            )
        } catch (e: Exception) {
            ResponseEntity(
                GeneralResponse(500, "Error updating order: ${e.message}"),
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
    }
}
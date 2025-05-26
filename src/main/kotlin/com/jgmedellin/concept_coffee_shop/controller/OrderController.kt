package com.jgmedellin.concept_coffee_shop.controller

import com.jgmedellin.concept_coffee_shop.dto.OrderDTO
import com.jgmedellin.concept_coffee_shop.dto.UpdateOrderDTO
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import com.jgmedellin.concept_coffee_shop.service.OrderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Orders", description = "These endpoints allow you to Create, Read, Update and Delete orders")
@RestController
@RequestMapping("/api/v1/orders", produces = [MediaType.APPLICATION_JSON_VALUE])
@Validated
class OrderController(val orderService: OrderService) {

    @Operation(
        summary = "Create an order",
        description = "Endpoint to create a new order",
        security = [SecurityRequirement(name = "bearerAuth")] // adds security requirement for JWT authentication
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Order created successfully"),
        ApiResponse(responseCode = "400", description = "Bad Request"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "404", description = "Not Found"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @PostMapping()
    fun createOrder(@RequestBody @Valid orderDTO: OrderDTO): ResponseEntity<GeneralResponse> =
        orderService.createOrder(orderDTO)

    @Operation(
        summary = "Get logged in user orders",
        description = "Endpoint to retrieve all orders for the authenticated user",
        security = [SecurityRequirement(name = "bearerAuth")] // adds security requirement for JWT authentication
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "404", description = "Not Found"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @GetMapping("/history")
    fun getMyOrders(): ResponseEntity<GeneralResponse> = orderService.getMyOrders()

    @Operation(
        summary = "Get all orders",
        description = "Endpoint to retrieve all orders. Accepts parameters for pagination and filtering.",
        security = [SecurityRequirement(name = "bearerAuth")] // adds security requirement for JWT authentication
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "404", description = "Not Found"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @GetMapping()
    fun getAllOrders(@RequestParam(defaultValue = "10") size: Int, @RequestParam(required = false) email: String?):
            ResponseEntity<GeneralResponse> = orderService.getAllOrders(size, email)

    @Operation(
        summary = "Delete an order",
        description = "Endpoint to delete an order by its ID",
        security = [SecurityRequirement(name = "bearerAuth")] // adds security requirement for JWT authentication
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Order deleted successfully"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "404", description = "Not Found"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @DeleteMapping("/{id}")
    fun deleteOrder(@PathVariable id: String): ResponseEntity<GeneralResponse> = orderService.deleteOrder(id)

    @Operation(
        summary = "Update an order",
        description = "Endpoint to update an order by its ID",
        security = [SecurityRequirement(name = "bearerAuth")] // adds security requirement for JWT authentication
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Order updated successfully"),
        ApiResponse(responseCode = "400", description = "Bad Request"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "404", description = "Not Found"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @PatchMapping("/{id}")
    fun updateOrder(@PathVariable id: String, @RequestBody @Valid updateOrderDTO: UpdateOrderDTO) :
            ResponseEntity<GeneralResponse> = orderService.updateOrder(id, updateOrderDTO)
}
package com.jgmedellin.concept_coffee_shop.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class OrderDTO (
    val id: String? = null,

    @get:NotBlank(message = "User email cannot be blank")
    val customerEmail: String,

    @get:NotEmpty(message = "Products list must not be empty")
    val orderItems: List<String>,

    @get:NotNull(message = "Total amount must not be null")
    @get:DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0.0")
    val totalAmount: Double


)
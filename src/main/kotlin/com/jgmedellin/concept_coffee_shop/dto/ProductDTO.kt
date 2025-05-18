package com.jgmedellin.concept_coffee_shop.dto

import com.jgmedellin.concept_coffee_shop.constants.ProductCategories
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class ProductDTO (
    val id: Int? = null,

    @get:NotBlank(message = "Product name cannot be blank")
    val name: String,

    @get:NotBlank(message = "Product description cannot be blank")
    val description: String,

    @get:NotNull(message = "Product price cannot be null")
    @get:DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0.0")
    @get:DecimalMax(value = "99.99", inclusive = true, message = "Price must be less than or equal to 99.99")
    val price: Double,

    @get:NotNull(message = "Product category cannot be null")
    val category: ProductCategories
)
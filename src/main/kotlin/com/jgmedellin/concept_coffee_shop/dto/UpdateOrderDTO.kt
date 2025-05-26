package com.jgmedellin.concept_coffee_shop.dto

import com.jgmedellin.concept_coffee_shop.constants.OrderStatus

data class UpdateOrderDTO (
    var orderItems: List<String>? = null, // List of product IDs as strings
    var orderStatus: OrderStatus
)
package com.jgmedellin.concept_coffee_shop.repository

import com.jgmedellin.concept_coffee_shop.entity.Order
import org.springframework.data.repository.CrudRepository

interface OrderRepository : CrudRepository<Order, String>{
    /**
     * Find all orders by customer email.
     * @param email the email of the customer.
     * @return a list of orders associated with the given email.
     */
    fun findAllByUserEmail(email: String): List<Order>
}
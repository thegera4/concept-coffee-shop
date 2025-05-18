package com.jgmedellin.concept_coffee_shop.repository

import com.jgmedellin.concept_coffee_shop.entity.Product
import org.springframework.data.repository.CrudRepository

interface ProductRepository : CrudRepository<Product, Int> {
    /**
     * Check if a product with the given name exists in the database.
     * @param name The name to check.
     * @return true if a product with the given name exists, false otherwise.
     */
    fun existsByName(name: String): Boolean

    /**
     * Find a product by its name.
     * @param name The name of the product to find.
     * @return The product with the given name, or null if no such product exists.
     */
    fun findByName(name: String): Product?
}
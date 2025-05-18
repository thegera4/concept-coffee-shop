package com.jgmedellin.concept_coffee_shop.service

import com.jgmedellin.concept_coffee_shop.dto.ProductDTO
import com.jgmedellin.concept_coffee_shop.entity.Product
import com.jgmedellin.concept_coffee_shop.repository.ProductRepository
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class ProductService(val productRepository: ProductRepository) {

    /**
     * Product service method to create new products.
     * @param productDTO the product data transfer object containing product details.
     * @return GeneralResponse with the operation result
     */
    fun createProducts(productsDTOs: List<ProductDTO>): GeneralResponse {
        // Check if any of the products already exist
        productsDTOs.forEach { productDTO ->
            if (productRepository.existsByName(productDTO.name)) {
                return GeneralResponse(HttpStatus.BAD_REQUEST, "Product ${productDTO.name} already exists!", null)
            }
        }
        // Create product entities and save them to the database
        try {
            // Convert DTOs to entities and save them to the database
            val createdProducts = productsDTOs.map { productDTO ->
                val product = Product(name = productDTO.name, description = productDTO.description,
                    price = productDTO.price, category = productDTO.category)
                productRepository.save(product)
            }

            // Return success response with created product details
            return GeneralResponse(
                HttpStatus.CREATED,
                "Products created successfully",
                createdProducts.map {
                    mapOf("id" to it.id, "name" to it.name, "price" to it.price, "category" to it.category)
                }
            )
        } catch (e: Exception) {
            return GeneralResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create products: ${e.message}", null)
        }
    }

    /**
     * Product service method to get all products.
     * @return GeneralResponse with the operation result
     */
    fun getAllProducts(): GeneralResponse {
        // Retrieve all products from the database
        val products = productRepository.findAll()
        // Return success response with product list
        return GeneralResponse(
            HttpStatus.OK, "Products retrieved successfully",
            products.map {
                mapOf("id" to it.id, "name" to it.name, "price" to it.price,
                    "category" to it.category, "description" to it.description
                )
            }
        )
    }
}
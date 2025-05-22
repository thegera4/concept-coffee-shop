package com.jgmedellin.concept_coffee_shop.service

import com.jgmedellin.concept_coffee_shop.dto.ProductDTO
import com.jgmedellin.concept_coffee_shop.entity.Product
import com.jgmedellin.concept_coffee_shop.repository.ProductRepository
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class ProductService(val productRepository: ProductRepository) {

    /**
     * Product service method to create new products.
     * @param productsDTOs the product(s) data transfer object(s) containing product(s) details.
     * @return GeneralResponse with the operation result
     */
    fun createProducts(productsDTOs: List<ProductDTO>): ResponseEntity<GeneralResponse> {
        // Find duplicates in the request
        val duplicateNames = productsDTOs.groupBy { it.name }.filter { it.value.size > 1 }.keys
        if (duplicateNames.isNotEmpty()) {
            return ResponseEntity(
                GeneralResponse(
                    400,
                    "Duplicate product names in request: ${duplicateNames.joinToString(", ")}"
                ),
                HttpStatus.BAD_REQUEST
            )
        }
        // Check which products already exist in the database
        val existingNames = productsDTOs.map { it.name }.filter { productRepository.existsByName(it) }
        if (existingNames.isNotEmpty()) {
            return ResponseEntity(
                GeneralResponse(
                    400,
                    "Product(s) already exist: ${existingNames.joinToString(", ")}"
                ),
                HttpStatus.BAD_REQUEST
            )
        }
        // Convert DTOs to entities
        val products = productsDTOs.map { productDTO ->
            Product(name = productDTO.name, description = productDTO.description, price = productDTO.price,
                category = productDTO.category)
        }
        return try {
            val createdProducts = productRepository.saveAll(products)
            ResponseEntity(
                GeneralResponse(201, "Products created successfully",
                    createdProducts.map {
                        mapOf(
                            "id" to it.id, "name" to it.name, "price" to it.price,
                            "category" to it.category, "description" to it.description
                        )
                    }
                ),
                HttpStatus.CREATED
            )
        } catch (e: Exception) {
            ResponseEntity(
                GeneralResponse(500, "Error creating products: ${e.message}"),
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
    }

    /**
     * Product service method to get all products.
     * @return GeneralResponse with the operation result
     */
    fun getAllProducts(): ResponseEntity<GeneralResponse> {
        // Retrieve all products from the database
        val products = productRepository.findAll()
        // Return success response with product list
        return ResponseEntity(
            GeneralResponse(200, "Products retrieved successfully",
                products.map {
                    mapOf("id" to it.id, "name" to it.name, "price" to it.price,
                        "category" to it.category, "description" to it.description
                    )
                }
            ),
            HttpStatus.OK
        )
    }

    /**
     * Product service method to get a product by ID.
     * @param id the ID of the product to retrieve.
     * @return GeneralResponse with the operation result
     */
    fun getProductById(id: Int): ResponseEntity<GeneralResponse> {
        // Check if the product exists in the database
        val product = productRepository.findById(id)
        // If the product exists, return success response with product details
        return if (product.isPresent) {
            ResponseEntity(
                GeneralResponse(
                    200, "Product retrieved successfully",
                    mapOf(
                        "name" to product.get().name, "price" to product.get().price,
                        "category" to product.get().category, "description" to product.get().description
                    )
                ),
                HttpStatus.OK
            )
        } else {
            ResponseEntity(
                GeneralResponse(404, "Product not found"),
                HttpStatus.NOT_FOUND
            )
        }
    }

    /**
     * Product service method to delete a product by ID.
     * @param id the ID of the product to delete.
     * @return GeneralResponse with the operation result
     */
    fun deleteProduct(id: Int): ResponseEntity<GeneralResponse> {
        // Check if the product exists in the database
        val product = productRepository.findById(id)
        // If the product exists, delete it and return success response
        return if (product.isPresent) {
            productRepository.delete(product.get())
            ResponseEntity(
                GeneralResponse(200, "Product deleted successfully"),
                HttpStatus.OK
            )
        } else {
            ResponseEntity(
                GeneralResponse(404, "Product not found"),
                HttpStatus.NOT_FOUND
            )
        }
    }

}
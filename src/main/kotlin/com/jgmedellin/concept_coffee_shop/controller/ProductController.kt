package com.jgmedellin.concept_coffee_shop.controller

import com.jgmedellin.concept_coffee_shop.dto.ProductDTO
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import com.jgmedellin.concept_coffee_shop.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.security.SecurityRequirement
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
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Products", description = "These endpoints allow you to Create, Read, Update and Delete products")
@RestController
@RequestMapping("/api/v1/products", produces = [MediaType.APPLICATION_JSON_VALUE])
@Validated
class ProductController(val productService: ProductService) {

    @Operation(
        summary = "Create Product(s)",
        description = "Endpoint to create new products",
        security = [SecurityRequirement(name = "bearerAuth")] // adds security requirement for JWT authentication
        )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Product(s) created successfully"),
        ApiResponse(responseCode = "400", description = "Bad Request"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @PostMapping()
    fun createProducts(@RequestBody @Valid productsDTOs: List<ProductDTO>) : ResponseEntity<GeneralResponse> =
        productService.createProducts(productsDTOs)

    @Operation(
        summary = "Get All Products",
        description = "Endpoint to get all products",
        security = [SecurityRequirement(name = "bearerAuth")] // adds security requirement for JWT authentication
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @GetMapping()
    fun getAllProducts() : ResponseEntity<GeneralResponse> = productService.getAllProducts()

    @Operation(
        summary = "Get product details",
        description = "Endpoint to get product details by ID",
        security = [SecurityRequirement(name = "bearerAuth")] // adds security requirement for JWT authentication
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Int): ResponseEntity<GeneralResponse> = productService.getProductById(id)

    @Operation(
        summary = "Delete a product",
        description = "Endpoint to delete a product by ID",
        security = [SecurityRequirement(name = "bearerAuth")] // adds security requirement for JWT authentication
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Product deleted successfully"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Int) : ResponseEntity<GeneralResponse> = productService.deleteProduct(id)

    @Operation(
        summary = "Update product details",
        description = "Endpoint to update product details by ID",
        security = [SecurityRequirement(name = "bearerAuth")] // adds security requirement for JWT authentication
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Product updated successfully"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @PatchMapping("/{id}")
    fun updateProduct(@PathVariable id: Int, @RequestBody @Valid productDTO: ProductDTO) :
            ResponseEntity<GeneralResponse> = productService.updateProduct(id, productDTO)

}
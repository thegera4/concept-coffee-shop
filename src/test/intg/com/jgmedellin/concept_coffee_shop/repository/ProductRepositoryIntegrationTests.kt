package com.jgmedellin.concept_coffee_shop.repository

import com.jgmedellin.concept_coffee_shop.CoffeeShopApplication
import com.jgmedellin.concept_coffee_shop.constants.ProductCategories
import com.jgmedellin.concept_coffee_shop.entity.Product
import com.jgmedellin.concept_coffee_shop.util.PostgreSQLContainerInitializer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@DataJpaTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = [CoffeeShopApplication::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryIntegrationTests : PostgreSQLContainerInitializer() {

    @Autowired
    lateinit var productRepository: ProductRepository

    @BeforeEach
    fun setup() {
        productRepository.deleteAll()
        val product1 = Product(
            name = "Espresso", description = "Strong coffee", price = 2.50,
            images = listOf("https://example.com/espresso.jpg"), category = ProductCategories.DRINK,
            isRecommended = true, isBestSeller = true
        )
        val product2 = Product(
            name = "Donut", description = "Regular maple covered donut. A true classic.", price = 1.50,
            images = listOf("https://example.com/donut.jpg"), category = ProductCategories.FOOD,
            isRecommended = true, isBestSeller = false
        )
        productRepository.saveAll(listOf(product1, product2))
    }

    @Test
    fun existsByName() {
        val productExists = productRepository.existsByName("Espresso")
        Assertions.assertEquals(true, productExists)
        val productDoesNotExist = productRepository.existsByName("Latte")
        Assertions.assertEquals(false, productDoesNotExist)
    }

}
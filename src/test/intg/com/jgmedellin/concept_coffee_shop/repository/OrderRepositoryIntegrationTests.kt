package com.jgmedellin.concept_coffee_shop.repository

import com.jgmedellin.concept_coffee_shop.CoffeeShopApplication
import com.jgmedellin.concept_coffee_shop.constants.ProductCategories
import com.jgmedellin.concept_coffee_shop.constants.UserRoles
import com.jgmedellin.concept_coffee_shop.entity.Order
import com.jgmedellin.concept_coffee_shop.entity.Product
import com.jgmedellin.concept_coffee_shop.entity.User
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
class OrderRepositoryIntegrationTests : PostgreSQLContainerInitializer() {

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var productRepository: ProductRepository

    private lateinit var testUser1: User
    private lateinit var testUser2: User
    private lateinit var testProduct1: Product
    private lateinit var testProduct2: Product

    @BeforeEach
    fun setup() {
        // Clean up any existing data
        orderRepository.deleteAll()
        userRepository.deleteAll()
        productRepository.deleteAll()

        // Create test users
        testUser1 = User(
            email = "user1@test.com",
            password = "password1",
            role = UserRoles.USER
        )
        testUser2 = User(
            email = "user2@test.com",
            password = "password2",
            role = UserRoles.USER
        )
        userRepository.save(testUser1)
        userRepository.save(testUser2)

        // Create test products
        testProduct1 = Product(
            name = "Coffee",
            description = "Hot coffee",
            price = 3.5,
            category = ProductCategories.DRINK,
            images = listOf("coffee.jpg")
        )
        testProduct2 = Product(
            name = "Croissant",
            description = "Buttery croissant",
            price = 2.5,
            category = ProductCategories.FOOD,
            images = listOf("croissant.jpg")
        )
        productRepository.save(testProduct1)
        productRepository.save(testProduct2)

        // Create orders for testUser1
        val order1 = Order(
            user = testUser1,
            products = mutableListOf(testProduct1, testProduct2),
            totalAmount = testProduct1.price + testProduct2.price
        )
        val order2 = Order(
            user = testUser1,
            products = mutableListOf(testProduct1),
            totalAmount = testProduct1.price
        )

        // Create an order for testUser2
        val order3 = Order(
            user = testUser2,
            products = mutableListOf(testProduct2),
            totalAmount = testProduct2.price
        )

        orderRepository.saveAll(listOf(order1, order2, order3))
    }

    @Test
    fun findAllByUserEmail() {
        // When - retrieve orders for testUser1 by email
        val user1Orders = orderRepository.findAllByUserEmail(testUser1.email)

        // Then - verify that the correct number of orders is returned
        Assertions.assertEquals(2, user1Orders.size, "Should return 2 orders for user1")

        // Verify all orders belong to testUser1
        user1Orders.forEach { order ->
            Assertions.assertEquals(testUser1.email, order.user.email,
                "All orders should belong to user1")
        }

        // When - retrieve orders for testUser2 by email
        val user2Orders = orderRepository.findAllByUserEmail(testUser2.email)

        // Then - verify that the correct number of orders is returned
        Assertions.assertEquals(1, user2Orders.size, "Should return 1 order for user2")

        // Verify the order belongs to testUser2
        Assertions.assertEquals(testUser2.email, user2Orders[0].user.email,
            "Order should belong to user2")

        // When - retrieve orders for a non-existent user email
        val nonExistentUserOrders = orderRepository.findAllByUserEmail("nonexistent@test.com")

        // Then - verify that an empty list is returned
        Assertions.assertTrue(nonExistentUserOrders.isEmpty(),
            "Should return an empty list for non-existent user")
    }
}


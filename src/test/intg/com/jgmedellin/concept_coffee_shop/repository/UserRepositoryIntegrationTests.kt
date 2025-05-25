package com.jgmedellin.concept_coffee_shop.repository

import com.jgmedellin.concept_coffee_shop.CoffeeShopApplication
import com.jgmedellin.concept_coffee_shop.constants.UserRoles
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
class UserRepositoryIntegrationTests : PostgreSQLContainerInitializer() {

    @Autowired
    lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()

        val user1 = User(username = "John Doe", email = "test@email.com", password = "Pass123#",
            phone = "1234567890", address = "123 Main St", city = "Springfield", role = UserRoles.SUPER
        )
        val user2 = User(username = "Jane Doe", email = "test2@email.com", password = "Pass123#",
            phone = "0987654321", address = "456 Elm St", city = "Springfield", role = UserRoles.USER
        )
        userRepository.saveAll(listOf(user1, user2))
    }

    @Test
    fun existsByEmail () {
        val emailExists = userRepository.existsByEmail("test@email.com")
        Assertions.assertEquals(true, emailExists)
        val emailNotExists = userRepository.existsByEmail("test3@email.com")
        Assertions.assertEquals(false, emailNotExists)
    }

    @Test
    fun findByEmail() {
        val foundUser = userRepository.findByEmail("test@email.com")
        Assertions.assertEquals("John Doe", foundUser?.username)
        val userNotFound = userRepository.findByEmail("test3@email.com")
        Assertions.assertEquals(null, userNotFound)
    }

    @Test
    fun deleteByEmail() {
        val deletedUser = userRepository.deleteByEmail("test2@email.com")
        Assertions.assertEquals(1, deletedUser)
        val userNotFound = userRepository.findByEmail("test3@email.com")
        Assertions.assertEquals(null, userNotFound)
    }

}
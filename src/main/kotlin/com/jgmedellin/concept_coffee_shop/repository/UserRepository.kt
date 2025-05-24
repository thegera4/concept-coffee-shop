package com.jgmedellin.concept_coffee_shop.repository

import com.jgmedellin.concept_coffee_shop.entity.User
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Int> {
    /**
     * Check if a user with the given email exists in the database.
     * @param email The email to check.
     * @return true if a user with the given email exists, false otherwise.
     */
    fun existsByEmail(email: String): Boolean

    /**
     * Find a user by their email.
     * @param email The email of the user to find.
     * @return The user with the given email, or null if no such user exists.
     */
    fun findByEmail(email: String): User?

    /**
     * Delete a user by their email.
     * @param email The email of the user to delete.
     */
    fun deleteByEmail(email: String)
}
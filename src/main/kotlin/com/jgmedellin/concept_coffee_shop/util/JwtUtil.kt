package com.jgmedellin.concept_coffee_shop.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JwtUtil {

    @Value("\${jwt.secret}")
    lateinit var secret: String

    // Token validity set to 2 hours (in milliseconds)
    val validity: Long = 2 * 60 * 60 * 1000

    /**
     * Generates a JWT token for the given email.
     * @param email The email of the user for whom the token is generated.
     * @param role The role of the user (e.g., "USER", "ADMIN").
     * @return The generated JWT token as a String.
     */
    fun generateToken(email: String, role: String): String {
       return Jwts.builder()
           .subject(email)
           .claim("role", role)
           .signWith(Keys.hmacShaKeyFor(secret.toByteArray()))
           .issuedAt(java.util.Date())
           .expiration(java.util.Date(System.currentTimeMillis() + validity))
           .compact()
    }

    /**
     * Validates a JWT token.
     * @param token The token to validate.
     * @return true if the token is valid, false otherwise.
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.toByteArray()))
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Extracts the email from a JWT token.
     * @param token The token from which to extract the email.
     * @return The email contained in the token.
     */
    fun getEmailFromToken(token: String): String {
        return getClaims(token).subject
    }

    /**
     * Extracts the role from a JWT token.
     * @param token The token from which to extract the role.
     * @return The role contained in the token.
     */
    fun getRoleFromToken(token: String): String {
        return getClaims(token).get("role", String::class.java) ?: "USER"
    }

    /**
     * Extracts all claims from a JWT token.
     * @param token The token from which to extract the claims.
     * @return The claims contained in the token.
     */
    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.toByteArray()))
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
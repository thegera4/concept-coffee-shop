package com.jgmedellin.concept_coffee_shop.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class UserDTO (
    @get:NotBlank(message = "User email cannot be blank")
    @get:Email(message = "User email must be a valid email format")
    val email: String,

    @get:NotBlank(message = "User password cannot be blank")
    @get:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$!%^&+=])(?=\\S+$).{8,}$",
        message = "Password must be at least 8 characters with 1 uppercase, 1 lowercase, 1 number and 1 special character"
    )
    val password: String
)
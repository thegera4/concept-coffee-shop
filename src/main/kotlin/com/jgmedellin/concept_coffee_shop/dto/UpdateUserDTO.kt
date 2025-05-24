package com.jgmedellin.concept_coffee_shop.dto

import jakarta.validation.constraints.Pattern

data class UpdateUserDTO (
    @get:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$!%^&+=])(?=\\S+$).{8,}$",
        message = "Password must be at least 8 characters with 1 uppercase, 1 lowercase, 1 number and 1 special character"
    )
    var password: String? = null,

    var username: String? = null,

    var phone: String? = null,

    var address: String? = null,

    var city: String? = null,

    var avatar: String? = null,
)
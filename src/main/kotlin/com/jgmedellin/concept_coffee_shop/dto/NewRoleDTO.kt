package com.jgmedellin.concept_coffee_shop.dto

import com.jgmedellin.concept_coffee_shop.constants.UserRoles
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class NewRoleDTO (
    @get:NotBlank(message = "User email cannot be blank")
    val email: String,

    @get:NotNull(message = "New role must be provided")
    val role: UserRoles
)
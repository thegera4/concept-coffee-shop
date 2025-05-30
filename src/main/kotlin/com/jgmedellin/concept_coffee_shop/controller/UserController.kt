package com.jgmedellin.concept_coffee_shop.controller

import com.jgmedellin.concept_coffee_shop.dto.NewRoleDTO
import com.jgmedellin.concept_coffee_shop.dto.UpdateUserDTO
import com.jgmedellin.concept_coffee_shop.dto.UserDTO
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import com.jgmedellin.concept_coffee_shop.service.UserService
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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Users", description = "These endpoints allow you to Create, Read, Update and Delete users")
@RestController
@RequestMapping("/api/v1/users", produces = [MediaType.APPLICATION_JSON_VALUE])
@Validated
class UserController(val userService: UserService) {

    @Operation(summary = "Register User", description = "Endpoint to register a new user")
    @ApiResponses(ApiResponse(responseCode = "201", description = "User registered successfully"),
        ApiResponse(responseCode = "400", description = "Bad Request"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @PostMapping("/register")
    fun registerUser(@RequestBody @Valid userDTO: UserDTO) : ResponseEntity<GeneralResponse> =
        userService.registerUser(userDTO)

    @Operation(summary = "Login", description = "Endpoint to login a user")
    @ApiResponses(ApiResponse(responseCode = "200", description = "User logged in successfully"),
        ApiResponse(responseCode = "400", description = "Bad Request"),
        ApiResponse(responseCode = "401", description = "Invalid credentials"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @PostMapping("/login")
    fun login(@RequestBody @Valid userDTO: UserDTO) : ResponseEntity<GeneralResponse> =
        userService.loginUser(userDTO)

    @Operation(
        summary = "Change Role",
        description = "Endpoint to change the role of a user",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(ApiResponse(responseCode = "200", description = "User role changed successfully"),
        ApiResponse(responseCode = "400", description = "Bad Request"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @PatchMapping("/changeRole")
    fun changeRole(@RequestBody @Valid newRoleDTO: NewRoleDTO) : ResponseEntity<GeneralResponse>  =
        userService.changeRole(newRoleDTO)

    @Operation(
        summary = "Get All Users",
        description = "Endpoint to get all registered users",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @GetMapping()
    fun getAllUsers() : ResponseEntity<GeneralResponse> = userService.getAllUsers()

    @Operation(
        summary = "Get a User",
        description = "Endpoint to get a single user by ID",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(ApiResponse(responseCode = "200", description = "User retrieved successfully"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "404", description = "User not found"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Int) : ResponseEntity<GeneralResponse> = userService.getUserById(id)

    @Operation(
        summary = "Delete a User",
        description = "Endpoint to delete a user by ID",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(ApiResponse(responseCode = "200", description = "User deleted successfully"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "404", description = "User not found"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Int) : ResponseEntity<GeneralResponse> = userService.deleteUser(id)

    @Operation(
        summary = "Update User",
        description = "Endpoint to update user details",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(ApiResponse(responseCode = "200", description = "User details updated successfully"),
        ApiResponse(responseCode = "400", description = "Bad Request"),
        ApiResponse(responseCode = "403", description = "Forbidden"),
        ApiResponse(responseCode = "404", description = "User not found"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Int, @RequestBody @Valid updateUserDTO: UpdateUserDTO) :
            ResponseEntity<GeneralResponse> = userService.updateUser(id, updateUserDTO)

}
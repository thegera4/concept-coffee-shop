package com.jgmedellin.concept_coffee_shop.service

import com.jgmedellin.concept_coffee_shop.constants.UserRoles
import com.jgmedellin.concept_coffee_shop.dto.NewRoleDTO
import com.jgmedellin.concept_coffee_shop.dto.UpdateUserDTO
import com.jgmedellin.concept_coffee_shop.dto.UserDTO
import com.jgmedellin.concept_coffee_shop.entity.User
import com.jgmedellin.concept_coffee_shop.repository.UserRepository
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import com.jgmedellin.concept_coffee_shop.util.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: UserRepository, val jwtUtil: JwtUtil) {
    /** Password encoder for hashing passwords */
    val passwordEncoder = BCryptPasswordEncoder()

    /**
     * User service method to register a new user.
     * @param userDTO the user data transfer object containing user email and password.
     * @return GeneralResponse with the operation result
     */
    fun registerUser(userDTO: UserDTO): ResponseEntity<GeneralResponse> {
        // Check if email already exists
        if (userRepository.existsByEmail(userDTO.email)) {
            return ResponseEntity(
                GeneralResponse(400,"User already exists."),
                HttpStatus.BAD_REQUEST
            )
        }
        // Hash the password before storing
        val hashedPassword = passwordEncoder.encode(userDTO.password)
        // Create user entity with hashed password
        val createdUser = User(email = userDTO.email, password = hashedPassword)
        // Save the user to the database
        userRepository.save(createdUser)
        // Return success response (don't include password in response)
        return ResponseEntity(
            GeneralResponse(201,"User registered successfully."),
            HttpStatus.CREATED
        )
    }

    /**
     * User service method to log in a user.
     * @param userDTO the user data transfer object containing user email and password.
     * @return GeneralResponse with the operation result
     */
    fun loginUser(userDTO: UserDTO): ResponseEntity<GeneralResponse> {
        // Check if user exists
        val user = userRepository.findByEmail(userDTO.email)
            ?: return ResponseEntity(GeneralResponse(404, "User does not exist.", null),
                HttpStatus.NOT_FOUND)
        // Check if password matches
        if (!passwordEncoder.matches(userDTO.password, user.password)) {
            return ResponseEntity(GeneralResponse(400, "Invalid credentials.", null),
                HttpStatus.BAD_REQUEST)
        }
        // If password is ok, generate JWT token
        val token = jwtUtil.generateToken(user.email, user.role.toString())
        // Return success response only with token
        return ResponseEntity(
            GeneralResponse(200,"User logged in successfully.", mapOf("token" to token)),
            HttpStatus.OK
        )
    }

    /**
     * User service method to change the role of a user.
     * @param newRoleDTO the new role data transfer object containing user email and new role.
     * @return GeneralResponse with the operation result
     */
    fun changeRole(newRoleDTO: NewRoleDTO): ResponseEntity<GeneralResponse> {
        // Check if user exists
        val user = userRepository.findByEmail(newRoleDTO.email) ?:
        return ResponseEntity(
            GeneralResponse(404, "User does not exists.", null),
            HttpStatus.NOT_FOUND
        )
        // Update the user's role
        user.role = when (newRoleDTO.role) {
            UserRoles.USER -> UserRoles.USER
            UserRoles.ADMIN -> UserRoles.ADMIN
            UserRoles.SUPER -> UserRoles.SUPER
        }
        // Save the updated user to the database
        val updatedUser = userRepository.save(user)
        // Return success response
        return ResponseEntity(
            GeneralResponse(200, "User role changed successfully.",
                mapOf("id" to updatedUser.id, "email" to updatedUser.email, "role" to updatedUser.role)),
            HttpStatus.OK
        )
    }

    /**
     * User service method to get all registered users.
     * @return GeneralResponse with the operation result
     */
    fun getAllUsers(): ResponseEntity<GeneralResponse> {
        // Retrieve all users from the database
        val users = userRepository.findAll()
        // Return success response with user list (don't include password in response)
        return ResponseEntity(
            GeneralResponse(200, "Users retrieved successfully.",
                users.map { mapOf("id" to it.id, "email" to it.email, "role" to it.role) }),
            HttpStatus.OK
        )
    }

    /**
     * User service method to get a single user by ID.
     * @param id the ID of the user to retrieve.
     * @return GeneralResponse with the operation result
     */
    fun getUserById(id: Int): ResponseEntity<GeneralResponse> {
        // Check if user exists
        val user = userRepository.findById(id).orElse(null) ?: return ResponseEntity(
            GeneralResponse(404, "User does not exist.", null),
            HttpStatus.NOT_FOUND
        )
        // Return success response
        return ResponseEntity(
            GeneralResponse(
                200, "User retrieved successfully.",
                mapOf("id" to user.id, "email" to user.email, "role" to user.role, "username" to user.username,
                    "phone" to user.phone, "address" to user.address, "city" to user.city, "avatar" to user.avatar)
            ),
            HttpStatus.OK
        )
    }

    /**
     * User service method to delete a user by ID.
     * @param id the ID of the user to delete.
     * @return GeneralResponse with the operation result
     */
    fun deleteUser(id: Int): ResponseEntity<GeneralResponse> {
        // Check if user exists
        val user = userRepository.findById(id).orElse(null) ?: return ResponseEntity(
            GeneralResponse(404, "User does not exist.", null),
            HttpStatus.NOT_FOUND
        )
        // Delete the user from the database
        userRepository.delete(user)
        // Return success response
        return ResponseEntity(
            GeneralResponse(200, "User deleted successfully.", null),
            HttpStatus.OK
        )
    }

    /**
     * User service method to update user details.
     * @param id the ID of the user to update.
     * @param updateUserDTO the user data transfer object containing updated user details.
     * @return GeneralResponse with the operation result
     */
    fun updateUser(id: Int, updateUserDTO: UpdateUserDTO): ResponseEntity<GeneralResponse> {
        // Get authenticated user's email from SecurityContext
        val authentication = SecurityContextHolder.getContext().authentication
        val authenticatedEmail = authentication?.name

        // Find the user by email
        val authenticatedUser = authenticatedEmail?.let { userRepository.findByEmail(it) }
        if (authenticatedUser == null || authenticatedUser.id != id) {
            return ResponseEntity(
                GeneralResponse(403, "You are not allowed to update other users.", null),
                HttpStatus.FORBIDDEN
            )
        }

        // Check if user exists
        val user = userRepository.findById(id).orElse(null) ?: return ResponseEntity(
            GeneralResponse(404, "User does not exist.", null),
            HttpStatus.NOT_FOUND
        )
        // Only update password if provided
        if (updateUserDTO.password != null) {
            user.password = passwordEncoder.encode(updateUserDTO.password)
        }
        user.username = updateUserDTO.username
        user.phone = updateUserDTO.phone
        user.address = updateUserDTO.address
        user.city = updateUserDTO.city
        user.avatar = updateUserDTO.avatar
        // Save the updated user to the database
        userRepository.save(user)
        // Return success response (don't include password in response)
        return ResponseEntity(
            GeneralResponse(200, "User details updated successfully."),
            HttpStatus.OK
        )
    }
}
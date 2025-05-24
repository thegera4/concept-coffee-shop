package com.jgmedellin.concept_coffee_shop.exception

import com.jgmedellin.concept_coffee_shop.response.GeneralErrorResponse
import com.jgmedellin.concept_coffee_shop.response.GeneralResponse
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(e: ConstraintViolationException): ResponseEntity<GeneralResponse> {
        val errorMessage = e.constraintViolations.joinToString(", ") {
            "${it.propertyPath}: ${it.message}"
        }
        return ResponseEntity(
            GeneralResponse(400, errorMessage),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): GeneralErrorResponse {
        val errors = mutableMapOf<String, String>()

        ex.bindingResult.fieldErrors.forEach { error ->
            val fieldName = error.field
            val errorMessage = error.defaultMessage ?: "Validation error"
            errors[fieldName] = errorMessage
        }

        return GeneralErrorResponse(
            400, HttpStatus.BAD_REQUEST, errors, LocalDateTime.now()
        )
    }

}
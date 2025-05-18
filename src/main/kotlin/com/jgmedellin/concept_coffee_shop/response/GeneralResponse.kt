package com.jgmedellin.concept_coffee_shop.response

import org.springframework.http.HttpStatus

/**
 * GeneralResponse is a generic response class used to standardize the API responses.
 * It contains a status, an optional message, and optional data.
 */
class GeneralResponse {
    var status: HttpStatus
    var message: String? = null
    var data: Any? = null

    constructor(status: HttpStatus, message: String?, data: Any?) {
        this.status = status
        this.message = message
        this.data = data
    }

    constructor(status: HttpStatus, message: String?) {
        this.status = status
        this.message = message
    }

    constructor(status: HttpStatus) {
        this.status = status
    }
}
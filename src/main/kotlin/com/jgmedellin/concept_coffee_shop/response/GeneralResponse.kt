package com.jgmedellin.concept_coffee_shop.response

class GeneralResponse {
    var code: Int
    var message: String
    var data: Any? = null

    constructor(statusCode: Int, message: String, data: Any?) {
        this.code = statusCode
        this.message = message
        this.data = data
    }

    constructor(statusCode: Int, message: String) {
        this.code = statusCode
        this.message = message
    }
}
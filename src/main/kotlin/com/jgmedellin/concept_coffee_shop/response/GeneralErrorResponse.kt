package com.jgmedellin.concept_coffee_shop.response

import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class GeneralErrorResponse {
    var statusCode: Int? = null
    var statusMsg: HttpStatus? = null
    var messages: MutableMap<String, String>? = null
    var timeStamp: LocalDateTime = LocalDateTime.now()

    constructor(statusCode: Int?, statusMsg: HttpStatus?, messages: MutableMap<String, String>?, timeStamp: LocalDateTime) {
        this.statusCode = statusCode
        this.statusMsg = statusMsg
        this.messages = messages
        this.timeStamp = timeStamp
    }

}
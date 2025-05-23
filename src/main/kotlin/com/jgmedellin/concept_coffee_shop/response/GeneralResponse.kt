package com.jgmedellin.concept_coffee_shop.response

data class GeneralResponse (
    var code: Int,
    var message: String,
    var data: Any? = null
)
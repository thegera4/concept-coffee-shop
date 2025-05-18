package com.jgmedellin.concept_coffee_shop

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<CoffeeShopApplication>().with(TestcontainersConfiguration::class).run(*args)
}

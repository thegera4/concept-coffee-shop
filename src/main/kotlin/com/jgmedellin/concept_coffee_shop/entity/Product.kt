package com.jgmedellin.concept_coffee_shop.entity

import com.jgmedellin.concept_coffee_shop.constants.ProductCategories
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Column

@Entity
@Table(name = "products")
class Product (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "name", nullable = false, unique = true)
    var name: String,

    @Column(name = "description", nullable = false)
    var description: String,

    @Column(name = "price", nullable = false)
    var price: Double,

    @Column(name = "category", nullable = false)
    var category: ProductCategories,

    @Column(name = "images", nullable = true)
    var images: List<String>? = emptyList(),

    @Column(name = "best_seller", nullable = false)
    var isBestSeller: Boolean = false,

    @Column(name = "recommended", nullable = false)
    var isRecommended: Boolean = false

) : BaseEntity()
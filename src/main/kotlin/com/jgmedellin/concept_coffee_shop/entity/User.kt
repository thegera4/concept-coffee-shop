package com.jgmedellin.concept_coffee_shop.entity

import com.jgmedellin.concept_coffee_shop.constants.UserRoles
import jakarta.persistence.*
import jakarta.validation.constraints.Email

@Entity
@Table(name = "users")
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "email", nullable = false, unique = true)
    @field:Email
    var email: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "username", nullable = true, unique = true)
    var username: String? = null,

    @Column(name = "phone", nullable = true, unique = true)
    var phone: String? = null,

    @Column(name = "address", nullable = true)
    var address: String? = null,

    @Column(name = "city", nullable = true)
    var city: String? = null,

    @Column(name = "role", nullable = true)
    @Enumerated(EnumType.STRING)
    var role: UserRoles = UserRoles.USER,

    @Column(name = "avatar", nullable = true)
    var avatar: String? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, targetEntity = Order::class)
    var orders: MutableList<Order> = mutableListOf()

) : BaseEntity()
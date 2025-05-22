package com.jgmedellin.concept_coffee_shop.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@MappedSuperclass // This annotation indicates that this class is a base class for entities
data class BaseEntity (
    @CreatedDate
    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(insertable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
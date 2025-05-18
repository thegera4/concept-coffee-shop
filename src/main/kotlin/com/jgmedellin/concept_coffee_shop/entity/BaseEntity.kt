package com.jgmedellin.concept_coffee_shop.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@MappedSuperclass // This annotation indicates that this class is a base class for entities
data class BaseEntity (
    @CreatedDate
    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @CreatedBy
    @Column(updatable = false)
    val createdBy: String? = null,

    @LastModifiedDate
    @Column(insertable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedBy
    @Column(insertable = false)
    val updatedBy: String? = null
)
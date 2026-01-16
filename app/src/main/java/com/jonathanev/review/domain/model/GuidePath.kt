package com.jonathanev.review.domain.model

@JvmInline
value class GuidePath(val value: String) {
    init {
        require(value.isNotBlank()) { "GuidePath no puede estar vacío" }
    }
}
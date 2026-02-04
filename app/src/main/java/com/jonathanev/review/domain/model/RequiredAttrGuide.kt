package com.jonathanev.review.domain.model

@JvmInline
value class RequiredAttrGuide(val value: String) {
    init {
        require(value.isNotBlank()) { "AttrGuide no puede estar vacío" }
    }
}
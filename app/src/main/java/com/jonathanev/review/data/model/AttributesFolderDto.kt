package com.jonathanev.review.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AttributesFolderDto(
    val name: String,
    val imgFolder: String,
    val color: Int
)
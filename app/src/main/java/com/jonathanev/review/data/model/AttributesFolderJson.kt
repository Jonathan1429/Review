package com.jonathanev.review.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AttributesFolderJson(
    val name: String,
    val imgFolder: String,
    val color: Int
)
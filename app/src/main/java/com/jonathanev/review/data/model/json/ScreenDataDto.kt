package com.jonathanev.review.data.model.json

import kotlinx.serialization.Serializable

@Serializable
data class ScreenDataDto (
    val name: String,
    val description: String = "",
    val imgFolder: String,
    val color: Int,
    val version: Int = 1
)
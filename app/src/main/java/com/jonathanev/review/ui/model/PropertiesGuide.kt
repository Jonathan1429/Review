package com.jonathanev.review.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class PropertiesGuide(
    val name: String,
    val description: String
)
package com.jonathanev.review.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class GuideUiModel(
    val version: GuideVersion,
    val nameGuide: String,
    val description: String
)
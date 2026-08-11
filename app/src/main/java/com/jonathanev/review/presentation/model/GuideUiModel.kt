package com.jonathanev.review.presentation.model

import com.jonathanev.review.core.attributes.UIConstants
import kotlinx.serialization.Serializable

@Serializable
data class GuideUiModel(
    val version: GuideVersion,
    val nameGuide: String,
    val description: String
) {
    val displayDescription: String
        get() = description.ifEmpty { UIConstants.DEFAULT_EMPTY_DESCRIPTION }
}
package com.jonathanev.review.presentation.model

import android.os.Parcelable
import com.jonathanev.review.core.attributes.UIConstants
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class GuideUiModel(
    val version: GuideVersion,
    val nameGuide: String,
    val description: String
) : Parcelable {
    val displayDescription: String
        get() = description.ifEmpty { UIConstants.DEFAULT_EMPTY_DESCRIPTION }
}
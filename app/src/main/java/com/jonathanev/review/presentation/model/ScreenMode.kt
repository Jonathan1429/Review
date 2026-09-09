package com.jonathanev.review.presentation.model

import kotlinx.serialization.Serializable

@Serializable
enum class ScreenMode {
    CREATING,
    EDITING,
    VIEWING
}
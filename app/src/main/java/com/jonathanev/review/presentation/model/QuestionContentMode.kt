package com.jonathanev.review.presentation.model

import kotlinx.serialization.Serializable

@Serializable
enum class QuestionContentMode {
    CREATING,
    EDITING
}
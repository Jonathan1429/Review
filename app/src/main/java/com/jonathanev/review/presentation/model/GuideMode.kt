package com.jonathanev.review.presentation.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface GuideMode {
    @Serializable
    data class Create(val nameGuide: String, val description: String) : GuideMode {
        override fun toString() = "Create_Mode"
    }

    @Serializable
    data class Edit(val nameGuide: String, val description: String, val posQuestion: Int) : GuideMode {
        override fun toString() = "Edit_Mode"
    }

    @Serializable
    data class Review(val nameGuide: String, val posQuestion: Int): GuideMode {
        override fun toString() = "Review_Mode"
    }
}
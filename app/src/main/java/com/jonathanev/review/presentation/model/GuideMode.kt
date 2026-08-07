package com.jonathanev.review.presentation.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface GuideMode {
    @Serializable
    data object Create : GuideMode {
        override fun toString() = "Create_Mode"
    }

    @Serializable
    data object Edit : GuideMode {
        override fun toString() = "Edit_Mode"
    }

    @Serializable
    data object Review : GuideMode {
        override fun toString() = "Review_Mode"
    }
}
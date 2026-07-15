package com.jonathanev.review.presentation.model

sealed interface GuideMode {
    data class Create(val nameGuide: String, val description: String) : GuideMode {
        override fun toString() = "Create Mode"
    }
    data class Edit(val nameGuide: String, val description: String, val posQuestion: Int) : GuideMode {
        override fun toString() = "Edit Mode"
    }
    data class Review(val nameGuide: String, val posQuestion: Int): GuideMode {
        override fun toString() = "Review Mode"
    }
}
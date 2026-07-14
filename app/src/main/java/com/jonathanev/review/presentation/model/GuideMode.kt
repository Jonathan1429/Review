package com.jonathanev.review.presentation.model

sealed interface GuideMode {
    data class Create(val nameGuide: String, val description: String) : GuideMode
    data class Edit(val nameGuide: String, val description: String, val posQuestion: Int) : GuideMode
    data class Review(val nameGuide: String, val posQuestion: Int): GuideMode
}
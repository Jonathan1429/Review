package com.jonathanev.review.presentation.model

sealed interface GuideMode {
    data class Create(val nameGuide: String, val description: String) : GuideMode
    data class Edit(val nameGuide: String, val description: String, val noQuestion: Int) : GuideMode
    data object Review: GuideMode
}
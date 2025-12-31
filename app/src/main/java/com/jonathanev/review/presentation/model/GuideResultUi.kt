package com.jonathanev.review.presentation.model

sealed class GuideResultUi {
    data class Success(val guideUiModel: GuideUiModel) : GuideResultUi()
    data class Error(val message: String) : GuideResultUi()
}
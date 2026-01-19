package com.jonathanev.review.presentation.files.model

sealed class GuideResultUi {
    data class Success(val guideUiModel: GuideUiModel) : GuideResultUi()
    data object Error: GuideResultUi()
}
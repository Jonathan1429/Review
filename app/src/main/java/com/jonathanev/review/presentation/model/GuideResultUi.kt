package com.jonathanev.review.presentation.model

import com.jonathanev.review.presentation.model.GuideUiModel

sealed class GuideResultUi {
    data class Success(val guideUiModel: GuideUiModel) : GuideResultUi()
    data object Error: GuideResultUi()
}
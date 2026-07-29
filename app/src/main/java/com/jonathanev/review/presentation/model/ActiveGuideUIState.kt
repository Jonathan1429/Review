package com.jonathanev.review.presentation.model

sealed class ActiveGuideUIState {
    data object Loading : ActiveGuideUIState()
    data object Error : ActiveGuideUIState()
    data class Success(val guide: GuideUiModel) : ActiveGuideUIState()
}
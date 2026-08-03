package com.jonathanev.review.presentation.state

import com.jonathanev.review.presentation.model.GuideUiModel

sealed interface GuidesUiState {
    data object Loading : GuidesUiState
    data object Empty : GuidesUiState
    data class Success(val guides: List<GuideUiModel>) : GuidesUiState
}
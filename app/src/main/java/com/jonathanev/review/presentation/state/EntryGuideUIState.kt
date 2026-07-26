package com.jonathanev.review.presentation.state

sealed interface EntryGuidesUiState {
    data object Loading : EntryGuidesUiState
    data object Empty : EntryGuidesUiState
    data object HasGuides : EntryGuidesUiState
}
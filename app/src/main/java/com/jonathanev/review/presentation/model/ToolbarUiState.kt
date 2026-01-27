package com.jonathanev.review.presentation.model

data class ToolbarUiState(
    val showBack: Boolean = false,
    val showSave: Boolean = false,
    val showSuccess: Boolean = false,
    val showCancel: Boolean = false
)
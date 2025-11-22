package com.jonathanev.review.Data.Model

data class FoldersUiState(
    val isLoading: Boolean = false,
    val folders: List<GuiaModel> = emptyList(),
    val error: String? = null
)

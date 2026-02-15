package com.jonathanev.review.presentation.state

import com.jonathanev.review.presentation.model.FolderUiModel

data class FoldersUiState(
    val isLoading: Boolean = false,
    val folders: List<FolderUiModel> = emptyList(),
    val error: String? = null
)
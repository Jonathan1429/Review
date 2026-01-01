package com.jonathanev.review.presentation.state

import com.jonathanev.review.presentation.folders.model.FolderUiModel

data class FoldersUiState(
    val isLoading: Boolean = false,
    val folders: List<FolderUiModel> = emptyList(),
    val error: String? = null
)

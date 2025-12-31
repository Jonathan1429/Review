package com.jonathanev.review.presentation.state

import com.jonathanev.review.presentation.model.FolderUI

data class FoldersUiState(
    val isLoading: Boolean = false,
    val folders: List<FolderUI> = emptyList(),
    val error: String? = null
)

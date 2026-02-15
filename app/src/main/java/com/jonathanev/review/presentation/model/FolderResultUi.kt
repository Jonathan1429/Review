package com.jonathanev.review.presentation.model

import com.jonathanev.review.presentation.model.FolderUiModel

sealed class FolderResultUi {
    data class Success(val folderUi: FolderUiModel) : FolderResultUi()
    data class Error(val message: String) : FolderResultUi()
}
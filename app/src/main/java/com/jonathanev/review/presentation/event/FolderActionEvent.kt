package com.jonathanev.review.presentation.event

import com.jonathanev.review.presentation.model.FolderUiModel

sealed class FolderActionEvent {
    data object DeleteFolderSuccess: FolderActionEvent()
    data object OpenFolder : FolderActionEvent()
    data class RenameFolder(val folder: FolderUiModel) : FolderActionEvent()
    data class ShowMessage(val text: String): FolderActionEvent()
}

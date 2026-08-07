package com.jonathanev.review.presentation.event

sealed class FolderActionEvent {
    data object DeleteFolderSuccess: FolderActionEvent()
    data object OpenFolder : FolderActionEvent()
    data class ShowMessage(val text: String): FolderActionEvent()
}
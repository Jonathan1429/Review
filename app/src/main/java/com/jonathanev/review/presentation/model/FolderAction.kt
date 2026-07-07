package com.jonathanev.review.presentation.model

import kotlinx.serialization.Serializable

@Serializable
sealed class FolderAction {
    @Serializable
    data object CreatingFolder : FolderAction()

    @Serializable
    data class RenamingFile(val fileName: String, val description: String) : FolderAction()

    @Serializable
    data object RenamingFolder : FolderAction()

    @Serializable
    data object CreatingFile : FolderAction()

    @Serializable
    data object MovingFile : FolderAction()

    @Serializable
    data object None : FolderAction()
}
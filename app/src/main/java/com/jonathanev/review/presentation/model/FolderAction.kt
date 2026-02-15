package com.jonathanev.review.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class FolderAction: Parcelable {
    @Parcelize
    data object CreatingFolder: FolderAction()

    @Parcelize
    data class RenamingFile(val fileName: String): FolderAction()

    @Parcelize
    data object RenamingFolder: FolderAction()

    @Parcelize
    data object CreatingFile: FolderAction()

    @Parcelize
    data object MovingFile: FolderAction()

    @Parcelize
    data object None: FolderAction()
}
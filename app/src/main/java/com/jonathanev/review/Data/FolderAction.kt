package com.jonathanev.review.Data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
sealed class FolderAction: Parcelable {
    @Parcelize
    data object CreatingFolder: FolderAction()

    @Parcelize
    data object RenamingFile: FolderAction()

    @Parcelize
    data object RenamingFolder: FolderAction()

    @Parcelize
    data object CreatingFile: FolderAction()

    @Parcelize
    data class MovingFile(val pathFile: File): FolderAction()

    @Parcelize
    data object None: FolderAction()
}
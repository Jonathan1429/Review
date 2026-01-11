package com.jonathanev.review.presentation.folders.model

import android.os.Parcelable
import com.jonathanev.review.domain.model.GuideDomainModel
import kotlinx.parcelize.Parcelize
import java.io.File

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
    data class MovingFile(val pathGuides: File, val pathImages: File, val guideDomain: GuideDomainModel): FolderAction()

    @Parcelize
    data object None: FolderAction()
}
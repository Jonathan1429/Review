package com.jonathanev.review.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
sealed interface FileFormMode : Parcelable {
    @Serializable
    @Parcelize
    data object CreatingFile: FileFormMode

    @Serializable
    @Parcelize
    data class RenameFile(val guideUiModel: GuideUiModel): FileFormMode

    @Serializable
    @Parcelize
    data object CreatingFolder: FileFormMode

    @Serializable
    @Parcelize
    data class RenameFolder(val folderUiModel: FolderUiModel) : FileFormMode
}

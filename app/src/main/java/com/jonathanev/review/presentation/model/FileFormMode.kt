package com.jonathanev.review.presentation.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface FileFormMode {
    @Serializable
    data object CreatingFile: FileFormMode

    @Serializable
    data class RenameFile(val guideUiModel: GuideUiModel): FileFormMode

    @Serializable
    data object CreatingFolder: FileFormMode
}
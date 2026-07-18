package com.jonathanev.review.presentation.model

sealed interface FileFormMode {
    data object CreatingFile: FileFormMode
    data class RenameFile(val guideUiModel: GuideUiModel): FileFormMode
    data object CreatingFolder: FileFormMode
}
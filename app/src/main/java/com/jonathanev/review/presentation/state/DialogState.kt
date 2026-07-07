package com.jonathanev.review.presentation.state

import com.jonathanev.review.presentation.model.GuideResultUi

sealed interface DialogState {
    data class OptionsMenu(val guide: GuideResultUi.Success) : DialogState
    data class ConfirmDelete(val guide: GuideResultUi.Success) : DialogState
}
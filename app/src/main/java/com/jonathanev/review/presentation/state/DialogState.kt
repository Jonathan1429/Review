package com.jonathanev.review.presentation.state

import com.jonathanev.review.presentation.model.GuideResultUi

sealed interface DialogState {
    data class OptionsMenu(val item: GuideResultUi.Success) : DialogState
    data class ConfirmDelete(val item: GuideResultUi.Success) : DialogState
}
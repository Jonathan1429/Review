package com.jonathanev.review.presentation.state

sealed interface ActionDialogState<out T> {
    data object Hidden : ActionDialogState<Nothing>
    data class OptionsMenu<out T>(val item: T) : ActionDialogState<T>
    data class ConfirmDelete<out T>(val item: T) : ActionDialogState<T>
}
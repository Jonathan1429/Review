package com.jonathanev.review.Data.Model.prueba

sealed class UiEvent {
    data class PaintTextColors(val text: String) : UiEvent()
    data class ShowMessage(val text: String) : UiEvent()
}
package com.jonathanev.review.Data.Model.prueba

sealed class UiStopEvent {
    //data class PaintTextColors(val text: String) : UiStopEvent()
    data class ShowMessage(val text: String) : UiStopEvent()
}
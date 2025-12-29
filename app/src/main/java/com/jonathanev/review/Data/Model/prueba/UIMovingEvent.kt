package com.jonathanev.review.Data.Model.prueba

sealed class UIMovingEvent {
    data class ShowMessage(val text: String): UIMovingEvent()
}
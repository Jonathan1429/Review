package com.jonathanev.review.data.Model.prueba

sealed class UIMovingEvent {
    data class ShowMessage(val text: String): UIMovingEvent()
}
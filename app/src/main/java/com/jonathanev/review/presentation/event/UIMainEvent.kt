package com.jonathanev.review.presentation.event

sealed class UIMainEvent {
    data class ErrorMessage(val error: String) : UIMainEvent()
}
package com.jonathanev.review.presentation.event

sealed class PrepareGuideEvent {
    data class ShowMessage(val text: String) : PrepareGuideEvent()
}
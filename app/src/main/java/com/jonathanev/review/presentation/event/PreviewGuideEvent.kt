package com.jonathanev.review.presentation.event

sealed class PreviewGuideEvent {
    data object Editing : PreviewGuideEvent()
    data object Review : PreviewGuideEvent()
    data class ShowError(val error: String) : PreviewGuideEvent()
}
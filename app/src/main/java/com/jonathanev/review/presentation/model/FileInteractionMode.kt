package com.jonathanev.review.presentation.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface FileInteractionMode {
    @Serializable
    data object Default : FileInteractionMode

    @Serializable
    data object MovingItem : FileInteractionMode
}
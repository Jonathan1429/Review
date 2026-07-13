package com.jonathanev.review.presentation.model

sealed interface FileInteractionMode {
    data object Default : FileInteractionMode
    data object MovingItem : FileInteractionMode
}
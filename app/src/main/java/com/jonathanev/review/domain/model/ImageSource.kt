package com.jonathanev.review.domain.model

sealed class ImageSource {
    data class MovingGuide(val oldPath: GuidePath): ImageSource()
    data class SaveGuide(val currentPath: GuidePath): ImageSource()
}
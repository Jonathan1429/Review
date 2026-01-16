package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuidePath

sealed class ImageSource {
    data class MovingGuide(val oldPath: GuidePath): ImageSource()
    data class SaveGuide(val currentPath: GuidePath): ImageSource()
}
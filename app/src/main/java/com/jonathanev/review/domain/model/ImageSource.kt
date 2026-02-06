package com.jonathanev.review.domain.model

sealed class ImageSource {
    data class MovingGuide(
        val oldRelativeGuidePath: RelativeGuidePath,
        val relativeGuidePath: RelativeGuidePath
    ) : ImageSource()

    data class Save(
        val relativeGuidePath: RelativeGuidePath
    ) : ImageSource()
}
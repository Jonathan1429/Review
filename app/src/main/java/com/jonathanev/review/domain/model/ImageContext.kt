package com.jonathanev.review.domain.model

sealed class ImageContext {
    data class MovingImage(override val oldRelativeGuidePath: RelativeGuidePath) : ImageContext(),
        HasOriginPath

    data object Save : ImageContext()
}
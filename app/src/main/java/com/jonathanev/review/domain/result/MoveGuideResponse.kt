package com.jonathanev.review.domain.result

sealed class MoveGuideResponse {
    data object ErrorPathGuide: MoveGuideResponse()
    data object ErrorPathImages: MoveGuideResponse()
    data object ErrorMovingGuide: MoveGuideResponse()
    data object ErrorMovingImages: MoveGuideResponse()
    data object Success: MoveGuideResponse()
}
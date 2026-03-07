package com.jonathanev.review.domain.result

sealed class ExistGuideV1Result {
    data object ExistGuide: ExistGuideV1Result()
    data object NoExistGuide: ExistGuideV1Result()
    data object Error: ExistGuideV1Result()
}
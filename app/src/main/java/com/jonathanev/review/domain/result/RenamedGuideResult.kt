package com.jonathanev.review.domain.result

sealed class RenamedGuideResult {
    data object Success: RenamedGuideResult()
    data object RenamedError: RenamedGuideResult()
    data object ImageError: RenamedGuideResult()
}
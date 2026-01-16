package com.jonathanev.review.domain.result

sealed class RenamedGuideResult {
    data object Sucess: RenamedGuideResult()
    data object RenamedError: RenamedGuideResult()
    data object ImageError: RenamedGuideResult()
}
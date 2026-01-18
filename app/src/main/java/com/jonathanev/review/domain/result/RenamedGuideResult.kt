package com.jonathanev.review.domain.result

sealed class RenamedGuideResult {
    data object Success: RenamedGuideResult()
    data object RenamedError: RenamedGuideResult()
    data object ImageError: RenamedGuideResult()

    data object GuidePathError: RenamedGuideResult()
    data object NotFound : RenamedGuideResult()
    data object InvalidFormat : RenamedGuideResult()
    data object UnknownError : RenamedGuideResult()
    data object Error : RenamedGuideResult()
}
package com.jonathanev.review.domain.result

sealed class DeleteGuideResult {
    data object DeleteSuccess: DeleteGuideResult()
    data object ErrorImage: DeleteGuideResult()
    data object ErrorGuide: DeleteGuideResult()
}
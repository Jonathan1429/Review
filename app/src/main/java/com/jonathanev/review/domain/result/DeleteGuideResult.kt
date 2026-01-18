package com.jonathanev.review.domain.result

sealed class DeleteGuideResult {
    data object DeleteSuccess: DeleteGuideResult()
    data object ErrorImage: DeleteGuideResult()
    data object ErrorGuide: DeleteGuideResult()

    data object Error: DeleteGuideResult()
    data object InvalidFormat: DeleteGuideResult()
    data object NotFound: DeleteGuideResult()
    data object UnknownError: DeleteGuideResult()
}
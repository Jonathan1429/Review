package com.jonathanev.review.domain.result

sealed class UpdateGuideResult {
    object Success : UpdateGuideResult()
    object ErrorPath : UpdateGuideResult()
    object ErrorUpdateGuide: UpdateGuideResult()
    object ImagesFailed: UpdateGuideResult()
    data class SaveFailed(
        val cause: SaveGuideError
    ) : UpdateGuideResult()
}
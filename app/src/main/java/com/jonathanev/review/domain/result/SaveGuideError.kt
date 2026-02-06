package com.jonathanev.review.domain.result

sealed class SaveGuideError {
    data object IOException: SaveGuideError()
    data object SecurityException: SaveGuideError()
    data object ErrorSave: SaveGuideError()
}
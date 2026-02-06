package com.jonathanev.review.domain.model

sealed class SaveGuideMode {
    data object Create : SaveGuideMode()
    data object Update : SaveGuideMode()
}
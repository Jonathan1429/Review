package com.jonathanev.review.presentation.model

sealed class SaveGuideMode {
    data object Create : SaveGuideMode()
    data object Update : SaveGuideMode()
}
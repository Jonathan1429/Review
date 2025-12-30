package com.jonathanev.review.data.Model

sealed class GuideResult {
    data class Success(val folder: GuideModel) : GuideResult()
    data class Error(val message: String) : GuideResult()
}
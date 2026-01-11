package com.jonathanev.review.domain.model

sealed class GuideSource {
    data object CurrentPath : GuideSource()
    data class CustomPath(val path: String) : GuideSource()
}


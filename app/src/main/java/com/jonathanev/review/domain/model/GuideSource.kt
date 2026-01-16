package com.jonathanev.review.domain.model

sealed class GuideSource {
    data class CurrentPath(val path: String) : GuideSource()
    data class CustomPath(val path: String) : GuideSource()
}


package com.jonathanev.review.domain.model

sealed class GuideSource {
    data class CurrentPath(val path: GuidePath) : GuideSource()
    data class SourcePath(val path: GuidePath) : GuideSource()
}
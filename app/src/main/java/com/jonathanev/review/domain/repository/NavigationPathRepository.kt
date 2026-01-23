package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuidePath

interface NavigationPathRepository {
    val currentPathGuides: GuidePath
    val currentPathImages: GuidePath

    fun next(fileName: String)
    fun back()
    fun reset()
}
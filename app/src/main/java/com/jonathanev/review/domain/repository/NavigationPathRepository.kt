package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuidePath

interface NavigationPathRepository {
    fun getPathImages(): GuidePath
    fun getPathGuides(): GuidePath
    fun next(fileName: String)
    fun back()
    fun reset()
}
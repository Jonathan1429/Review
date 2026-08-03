package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.RelativeGuidePath

interface NavigationPathRepository {
    suspend fun getRelativePath(): RelativeGuidePath
    fun getRootGuides(): GuidePath
    fun getRootImages(): GuidePath
    suspend fun next(fileName: String)
    suspend fun reset()
}
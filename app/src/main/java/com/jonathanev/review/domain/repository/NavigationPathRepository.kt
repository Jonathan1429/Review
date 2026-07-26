package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.RelativeGuidePath
import kotlinx.coroutines.flow.Flow

interface NavigationPathRepository {
    val relativePath: Flow<RelativeGuidePath>
    fun getRootGuides(): GuidePath
    fun getRootImages(): GuidePath
    suspend fun next(fileName: String)
    suspend fun back()
    suspend fun reset()
}
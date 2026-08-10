package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.RelativeGuidePath
import kotlinx.coroutines.flow.Flow

interface NavigationPathRepository {
    fun getRelativePathFlow(): Flow<RelativeGuidePath>
    suspend fun getRelativePath(): RelativeGuidePath
    fun getRootGuides(): GuidePath
    fun getRootImages(): GuidePath
    suspend fun next(fileName: String): Result<Unit>
    suspend fun reset(): Result<Unit>
}
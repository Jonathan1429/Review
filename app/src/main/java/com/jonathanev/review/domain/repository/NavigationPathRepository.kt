package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.RelativeGuidePath

interface NavigationPathRepository {
    fun getRootGuides(): GuidePath
    fun getRootImages(): GuidePath
    fun next(current: RelativeGuidePath, fileName: String): RelativeGuidePath
    fun back(current: RelativeGuidePath): RelativeGuidePath
}
package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideDomainModel
import kotlinx.coroutines.flow.Flow

interface ActiveGuideRepository {
    val activeGuideFlow: Flow<GuideDomainModel?>
    suspend fun setActiveGuide(guide: GuideDomainModel): Result<Unit>
    suspend fun clearActiveGuide(): Result<Unit>
}
package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import kotlinx.coroutines.flow.StateFlow

interface GuideMoveRepository {
    val guideContext: StateFlow<GuideContext.Moving?>
    suspend fun start(guideDomainModel: GuideDomainModel)
    fun clear()
}
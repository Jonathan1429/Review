package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideContext
import kotlinx.coroutines.flow.Flow

interface GuideContextRepository {
    val guideContext: Flow<GuideContext?>
    suspend fun start(guideContext: GuideContext)
    suspend fun clear()
}
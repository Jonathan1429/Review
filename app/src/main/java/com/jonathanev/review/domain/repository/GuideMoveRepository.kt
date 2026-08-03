package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel

interface GuideMoveRepository {
    suspend fun start(guideDomainModel: GuideDomainModel)
    fun get(): GuideContext.Moving?
    fun clear()
}
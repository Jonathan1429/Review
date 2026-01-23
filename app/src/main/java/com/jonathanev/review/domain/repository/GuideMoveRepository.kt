package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideContext

interface GuideMoveRepository {
    fun start(guide: GuideContext.Moving)
    fun get(): GuideContext.Moving?
    fun clear()
}
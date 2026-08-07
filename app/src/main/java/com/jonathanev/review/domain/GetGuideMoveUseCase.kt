package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.repository.GuideMoveRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetGuideMoveUseCase @Inject constructor(
    private val guideMoveRepository: GuideMoveRepository
) {
    operator fun invoke(): StateFlow<GuideContext.Moving?> {
        return guideMoveRepository.guideContext
    }
}
package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.repository.GuideContextRepository
import javax.inject.Inject

class SetContextMoveUseCase @Inject constructor(
    private val guideContextRepository: GuideContextRepository
) {
    suspend operator fun invoke(contextMoving: GuideContext.Moving): Result<Unit> {
        return guideContextRepository.start(contextMoving)
    }
}
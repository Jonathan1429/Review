package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.repository.GuideContextRepository
import javax.inject.Inject

class SetContextCreateUseCase @Inject constructor(
    private val guideContextRepository: GuideContextRepository
) {
    suspend operator fun invoke(contextCreate: GuideContext.Creating): Result<Unit> {
        return guideContextRepository.start(contextCreate)
    }
}
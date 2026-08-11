package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.repository.GuideContextRepository
import javax.inject.Inject

class SetContextPlayUseCase @Inject constructor(
    private val guideContextRepository: GuideContextRepository
) {
    suspend operator fun invoke(guideContextPlay: GuideContext.Browsing): Result<Unit> {
        return guideContextRepository.start(guideContextPlay)
    }
}
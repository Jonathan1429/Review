package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.repository.GuideContextRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGuideContextUseCase @Inject constructor(
    private val guideContextRepository: GuideContextRepository
) {
    operator fun invoke(): Flow<GuideContext?> {
        return guideContextRepository.guideContext
    }
}
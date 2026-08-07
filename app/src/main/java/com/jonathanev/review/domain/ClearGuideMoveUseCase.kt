package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.GuideContextRepository
import javax.inject.Inject

class ClearGuideMoveUseCase @Inject constructor(
    private val guideContextRepository: GuideContextRepository
) {
    suspend operator fun invoke() {
        guideContextRepository.clear()
    }
}
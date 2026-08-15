package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.ActiveGuideRepository
import javax.inject.Inject

class ClearActiveGuideUseCase @Inject constructor(
    private val activeGuideRepository: ActiveGuideRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return activeGuideRepository.clearActiveGuide()
    }
}
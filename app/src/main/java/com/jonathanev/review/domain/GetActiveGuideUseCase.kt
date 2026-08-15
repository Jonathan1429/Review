package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.ActiveGuideRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveGuideUseCase @Inject constructor(
    private val activateGuideRepository: ActiveGuideRepository
) {
    operator fun invoke(): Flow<GuideDomainModel?> {
        return activateGuideRepository.activeGuideFlow
    }
}
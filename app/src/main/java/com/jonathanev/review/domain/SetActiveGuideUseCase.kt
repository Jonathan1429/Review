package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.ActiveGuideRepository
import javax.inject.Inject

class SetActiveGuideUseCase @Inject constructor(
    private val activeGuideRepository: ActiveGuideRepository
) {
    suspend operator fun invoke(guideDomainModel: GuideDomainModel): Result<Unit> {
        return activeGuideRepository.setActiveGuide(guideDomainModel)
    }
}
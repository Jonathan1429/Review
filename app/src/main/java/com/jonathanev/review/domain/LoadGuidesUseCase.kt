package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.GuiaRepository
import javax.inject.Inject

class LoadGuidesUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
) {
    operator fun invoke(relativeGuidePath: RelativeGuidePath): List<GuideDomainModel> {
        return guiaRepository.getGuides(relativeGuidePath)
    }
}
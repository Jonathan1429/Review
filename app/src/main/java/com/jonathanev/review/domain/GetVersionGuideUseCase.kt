package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.GuideResource
import com.jonathanev.review.domain.result.ReadGuideError
import javax.inject.Inject

class GetVersionGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    suspend operator fun invoke(nameFile: String): GuideResource<GuideDomainModel, ReadGuideError> {
        return guiaRepository.getVersionGuide(nameFile)
    }
}
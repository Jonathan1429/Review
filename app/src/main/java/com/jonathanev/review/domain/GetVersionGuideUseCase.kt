package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.result.GuideResource
import com.jonathanev.review.domain.result.ReadGuideError
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetVersionGuideUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository,
    private val guiaRepository: GuiaRepository
) {
    suspend operator fun invoke(nameFile: String): GuideResource<GuideDomainModel, ReadGuideError> {
        val relativeGuidePath = navigationPathRepository.relativePath.first()
        return guiaRepository.getVersionGuide(nameFile, relativeGuidePath)
    }
}
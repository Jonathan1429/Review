package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.result.ExistGuideV1Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ExistXMLGuideV1UseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val navigationPathRepository: NavigationPathRepository
) {
    suspend operator fun invoke(guideDomainModel: GuideDomainModel): ExistGuideV1Result {
        val relativeGuidePath = navigationPathRepository.relativePath.first()
        return guiaRepository.existXMLGuideV1(guideDomainModel, relativeGuidePath)
    }
}
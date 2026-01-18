package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.presentation.navigation.NavigationPathRepository
import javax.inject.Inject

class LoadGuidesUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(): List<GuideDomainModel> {
        val guidesDomain = guiaRepository.getGuides(navigationPathRepository.currentPathGuides)
        return guidesDomain
    }
}
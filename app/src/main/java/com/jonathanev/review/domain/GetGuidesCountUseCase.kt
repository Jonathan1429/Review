package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class GetGuidesCountUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(): Int {
        return guiaRepository.getNumGuides(navigationPathRepository.currentPathGuides.value)
    }
}
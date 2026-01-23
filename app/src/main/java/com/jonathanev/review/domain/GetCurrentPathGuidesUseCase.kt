package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class GetCurrentPathGuidesUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke() = navigationPathRepository.currentPathGuides.value
}
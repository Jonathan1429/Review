package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class BackNavigationUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(relativeActual: String): RelativeGuidePath {
        return navigationPathRepository.back(RelativeGuidePath(relativeActual))
    }
}
package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class NextNavigationUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(relativeActual: String, folder: String): RelativeGuidePath {
        return navigationPathRepository.next(RelativeGuidePath(relativeActual), folder)
    }
}
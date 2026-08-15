package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class NextNavigationUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) {
    suspend operator fun invoke(folder: String): Result<Unit> {
        return navigationPathRepository.next(folder)
    }
}
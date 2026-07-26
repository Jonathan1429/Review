package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class ResetNavigationUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) {
    suspend operator fun invoke() {
        navigationPathRepository.reset()
    }
}
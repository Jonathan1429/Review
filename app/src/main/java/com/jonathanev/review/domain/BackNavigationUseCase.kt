package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class BackNavigationUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) {
    suspend operator fun invoke() {
        return navigationPathRepository.back()
    }
}
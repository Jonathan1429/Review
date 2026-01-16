package com.jonathanev.review.domain

import com.jonathanev.review.data.repository.NavigationPathRepository
import javax.inject.Inject

class NextPathUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(name: String) {
        navigationPathRepository.next(name)
    }
}
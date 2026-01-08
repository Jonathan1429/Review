package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class BackPathUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(){
        navigationPathRepository.back()
    }
}
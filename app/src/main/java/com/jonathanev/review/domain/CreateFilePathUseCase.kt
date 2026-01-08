package com.jonathanev.review.domain

import javax.inject.Inject

class CreateFilePathUseCase @Inject constructor(
    private val directoryManager: DirectoryManager
) {
    operator fun invoke(nameGuide: String) {
        directoryManager.createPathGuide(nameGuide)
    }
}
package com.jonathanev.review.Domain

import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.Domain.repository.FileRepository
import javax.inject.Inject

class SetMainPathUseCase @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val fileRepository: FileRepository
) {
    operator fun invoke() {
        val initialPath = filePathsProvider.fileGuides
        fileRepository.setCurrentPath(initialPath.path)
    }
}

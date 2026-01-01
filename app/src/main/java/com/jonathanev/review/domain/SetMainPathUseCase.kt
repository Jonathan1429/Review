package com.jonathanev.review.domain

import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.PathProvider
import javax.inject.Inject

class SetMainPathUseCase @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val pathProvider: PathProvider
) {
    operator fun invoke() {
        val initialPath = filePathsProvider.fileGuides
        pathProvider.setCurrentPath(initialPath.path)
    }
}

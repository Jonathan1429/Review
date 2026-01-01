package com.jonathanev.review.domain

import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.PathProvider
import java.io.File
import javax.inject.Inject

class ChangeGuidePathBuildFileUseCase @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val pathProvider: PathProvider
) {
    operator fun invoke(nameGuide: String): String {
        return filePathsProvider
            .buildFile(File(pathProvider.getCurrentPath()), nameGuide)
            .path
    }
}

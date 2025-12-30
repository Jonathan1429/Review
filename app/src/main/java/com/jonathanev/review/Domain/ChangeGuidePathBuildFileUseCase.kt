package com.jonathanev.review.Domain

import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.Domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class ChangeGuidePathBuildFileUseCase @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val fileRepository: FileRepository
) {
    operator fun invoke(nameGuide: String): String {
        return filePathsProvider
            .buildFile(File(fileRepository.getCurrentPath()), nameGuide)
            .path
    }
}

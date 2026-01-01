package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.data.provider.FilePathsProvider
import java.io.File
import javax.inject.Inject

class ChangeFilePathUseCase @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val pathProvider: PathProvider
) {
    operator fun invoke(folderName: String) {
        val newPath =
            filePathsProvider.buildFolder(File(pathProvider.getCurrentPath()), folderName)
                .toString()

        pathProvider.setCurrentPath(newPath)
    }
}
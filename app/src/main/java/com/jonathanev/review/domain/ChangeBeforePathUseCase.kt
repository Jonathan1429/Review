package com.jonathanev.review.domain

import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.PathProvider
import java.io.File
import javax.inject.Inject

class ChangeBeforePathUseCase @Inject constructor(
    private val pathProvider: PathProvider,
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke(){
        val currentPath = File(pathProvider.getCurrentPath())
        val beforePath = filePathsProvider.beforePath(currentPath)
        pathProvider.setCurrentPath(beforePath.path)
    }
}
package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.data.provider.FilePathsProvider
import javax.inject.Inject

class SetCurrentPathUseCase @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val pathProvider: PathProvider
) {
    operator fun invoke(){
        //pathProvider.setCurrentPath(filePathsProvider.fileGuides.path)
    }
}
package com.jonathanev.review.Domain

import com.jonathanev.review.data.provider.FilePathsProvider
import javax.inject.Inject

class CreateFoldersUseCase @Inject constructor(
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke(): Boolean {
        val paths = listOf(
            filePathsProvider.fileGuides,
            filePathsProvider.fileImages,
            //filePathsProvider.fileImagesPiv,
        )

        for (path in paths){
            if (!path.exists()){
                path.mkdir()
            }
        }
        return !(!paths[0].exists() || !paths[1].exists())
    }
}
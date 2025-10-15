package com.jonathanev.review.Domain

import androidx.appcompat.app.AlertDialog
import com.jonathanev.review.Data.Model.FilePathsProvider
import javax.inject.Inject

class CreateFoldersUseCase @Inject constructor(
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke(): Boolean {
        val paths = listOf(
            filePathsProvider.fileGuides,
            filePathsProvider.fileImages,
            filePathsProvider.fileImagesPiv
        )

        for (path in paths){
            if (!path.exists()){
                path.mkdir()
            }
        }
        return !(!paths[0].exists() || !paths[1].exists() || !paths[2].exists())
    }
}
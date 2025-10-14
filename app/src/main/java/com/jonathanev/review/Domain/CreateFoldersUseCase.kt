package com.jonathanev.review.Domain

import androidx.appcompat.app.AlertDialog
import com.jonathanev.review.Data.Model.FilePathsProvider
import javax.inject.Inject

class CreateFoldersUseCase @Inject constructor(
    private val filePathsProvider: FilePathsProvider
)  {
    operator fun invoke(): Boolean {
        if (!filePathsProvider.fileGuides.exists())
        {
            filePathsProvider.fileGuides.mkdir()
            return false
        }

        // Crear carpeta de imagenes
        if (!filePathsProvider.fileImages.exists())
        {
            filePathsProvider.fileImages.mkdirs()
            return false
        }

        if (!filePathsProvider.fileImagesPiv.exists())
        {
            filePathsProvider.fileImagesPiv.mkdirs()
            return false
        }

        return true
    }
}
package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants
import com.jonathanev.review.Core.Constants.IMAGENES
import com.jonathanev.review.Data.FileAction
import com.jonathanev.review.Domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class CreatingFolderUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    operator fun invoke(fileName: String): FileAction {
        val path = File(fileRepository.getCurrentPath(), fileName)
        val pathImages = File(fileRepository.getCurrentPath(), fileName).toString()
            .replace(Constants.GUIAS, IMAGENES)

        if (path.exists()) {
            return FileAction.EXIST
        }

        val createdImages = File(pathImages).mkdirs()
        val createdMain = path.mkdirs()

        return if (createdImages && createdMain) {
            FileAction.SUCCESS
        } else {
            FileAction.ERROR
        }
    }
}
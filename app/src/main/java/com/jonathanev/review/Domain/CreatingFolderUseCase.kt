package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants
import com.jonathanev.review.Core.Constants.IMAGENES
import com.jonathanev.review.Data.FileAction
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import java.io.File
import javax.inject.Inject

class CreatingFolderUseCase @Inject constructor(
    private val fileRepositoryImpl: FileRepositoryImpl
) {
    operator fun invoke(fileName: String): FileAction {
        val path = File(fileRepositoryImpl.getCurrentPath(), fileName)
        val pathImages = File(fileRepositoryImpl.getCurrentPath(), fileName).toString().replace(Constants.GUIAS, IMAGENES)

        if (path.exists()){
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
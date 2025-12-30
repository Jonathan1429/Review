package com.jonathanev.review.Domain

import com.jonathanev.review.Domain.repository.FileRepository
import com.jonathanev.review.data.FileAction
import com.jonathanev.review.data.storage.StorageFolders
import java.io.File
import javax.inject.Inject

class CreatingFolderUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    operator fun invoke(fileName: String): FileAction {
        val path = File(fileRepository.getCurrentPath(), fileName)
        val pathImages = File(fileRepository.getCurrentPath(), fileName).toString()
            .replace(StorageFolders.GUIAS, StorageFolders.IMAGENES)

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
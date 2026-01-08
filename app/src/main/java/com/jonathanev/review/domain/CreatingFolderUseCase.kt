package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.domain.model.FileAction
import com.jonathanev.review.data.storage.StorageFolders
import java.io.File
import javax.inject.Inject

class CreatingFolderUseCase @Inject constructor(
    private val pathProvider: PathProvider
) {
    operator fun invoke(fileName: String) {
        /*val path = File(pathProvider.getCurrentPath(), fileName)
        val pathImages = File(pathProvider.getCurrentPath(), fileName).toString()
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
        }*/
    }
}
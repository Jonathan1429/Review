package com.jonathanev.review.domain

import com.jonathanev.review.data.provider.FilePathsProvider
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject

class SetCopyImagesUseCase @Inject constructor(
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke(imgPiv: File = filePathsProvider.fileImagesPiv, imagenes: File = filePathsProvider.fileImages) {
        val images = imgPiv.listFiles()
        if (images != null) {
            for (archivo in images) {
                val name = archivo.name
                Files.copy(
                    archivo.toPath(),
                    imagenes.toPath().resolve(name),
                    StandardCopyOption.REPLACE_EXISTING
                )
                File(imgPiv, name).delete()
            }
        }
    }
}
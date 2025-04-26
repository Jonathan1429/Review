package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants.fileImages
import com.jonathanev.review.Core.Constants.fileImagesPiv
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject

class SetCopyImagesUseCase @Inject constructor() {
    operator fun invoke(imgPiv: File = fileImagesPiv, imagenes: File = fileImages) {
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
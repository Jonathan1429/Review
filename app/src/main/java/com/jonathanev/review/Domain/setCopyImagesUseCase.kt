package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants.fileImages
import com.jonathanev.review.Core.Constants.fileImagesPiv
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject

class setCopyImagesUseCase @Inject constructor() {
    operator fun invoke() {
        val images = fileImagesPiv.listFiles()
        // Hacemos un ciclo por cada fichero para extraer el nombre de cada uno.
        if (!images.isNullOrEmpty()) {
            for (i in images.indices) {
                // Sacamos del array files el primer fichero.
                val archivo: File = images[i]
                var name = ""

                name = archivo.name

                Files.copy(
                    Paths.get("$fileImagesPiv/$name"),
                    Paths.get("$fileImages/$name"),
                    StandardCopyOption.REPLACE_EXISTING
                )

                // Borrar archivo
                File(fileImagesPiv, name).delete()
            }
        }
    }
}
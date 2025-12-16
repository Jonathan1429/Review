package com.jonathanev.review.Domain

import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class MoveNonFolderFilesToOtrosUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke() {
        val currentPath = File(fileRepository.getCurrentPath())

        // Crear carpeta "Otros" si no existe
        if (!currentPath.listFiles().isNullOrEmpty()) {
            val otrosDir = File(currentPath, "Otros")

            if (!otrosDir.exists()) {
                otrosDir.mkdirs()
            }

            // Listar archivos que NO son carpetas
            val files = currentPath.listFiles()
                ?.filter { it.isFile }           // ❗ Solo archivos
                ?: emptyList()

            // Mover cada archivo
            files.forEach { file ->
                val newPath = File(otrosDir, file.name)
                file.renameTo(newPath)
            }

            val pathImage = File(currentPath.toString().replace("guias", "imagenes"))
            val otrosDirImage = File(pathImage, "Otros")

            if (!otrosDirImage.exists()) {
                otrosDirImage.mkdirs()
            }

            // Listar archivos que NO son carpetas
            val images = pathImage.listFiles()
                ?.filter { it.isFile }           // ❗ Solo archivos
                ?: emptyList()

            // Mover cada archivo
            images.forEach { file ->
                val newPath = File(otrosDirImage, file.name)
                file.renameTo(newPath)
            }
        }
    }
}
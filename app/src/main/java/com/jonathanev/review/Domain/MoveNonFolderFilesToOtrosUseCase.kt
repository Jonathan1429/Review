package com.jonathanev.review.Domain

import com.jonathanev.review.Data.repository.FileRepositoryImpl
import java.io.File
import javax.inject.Inject

class MoveNonFolderFilesToOtrosUseCase @Inject constructor(
    private val fileRepositoryImpl: FileRepositoryImpl
) {
    operator fun invoke(): Result<Unit> {
        return try {
            val currentPath = File(fileRepositoryImpl.getCurrentPath())
            if (!currentPath.exists()) return Result.failure(Exception("Path no existe"))

            // Crear carpeta "Otros" si no existe
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

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
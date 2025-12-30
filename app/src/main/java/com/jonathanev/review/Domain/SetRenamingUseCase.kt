package com.jonathanev.review.Domain

import com.jonathanev.review.data.FileAction
import com.jonathanev.review.Domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class SetRenamingUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    operator fun invoke(newFileName: String): FileAction {
        // Ruta + nombre del archivo.
        val routeWithoutFile = fileRepository.getCurrentPath().substringBeforeLast("/")
        val newPathFile = File("$routeWithoutFile/$newFileName")

        if (newPathFile.exists()){
            return FileAction.EXIST
        }

        val response = File(fileRepository.getCurrentPath()).renameTo(newPathFile)
        return if (response) FileAction.SUCCESS else FileAction.ERROR
    }
}
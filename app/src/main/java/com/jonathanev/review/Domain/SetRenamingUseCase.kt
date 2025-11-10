package com.jonathanev.review.Domain

import com.jonathanev.review.Data.FileAction
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import java.io.File
import javax.inject.Inject

class SetRenamingUseCase @Inject constructor(
    private val fileRepositoryImpl: FileRepositoryImpl
) {
    operator fun invoke(newFileName: String): FileAction {
        // Ruta + nombre del archivo.
        val routeWithoutFile = fileRepositoryImpl.getCurrentPath().substringBeforeLast("/")
        val newPathFile = File("$routeWithoutFile/$newFileName")

        if (newPathFile.exists()){
            return FileAction.EXIST
        }

        val response = File(fileRepositoryImpl.getCurrentPath()).renameTo(newPathFile)
        return if (response) FileAction.SUCCESS else FileAction.ERROR
    }
}
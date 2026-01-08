package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.FileAction
import com.jonathanev.review.domain.repository.PathProvider
import java.io.File
import javax.inject.Inject

class SetRenamingUseCase @Inject constructor(
    private val pathProvider: PathProvider
) {
    operator fun invoke(newFileName: String) {
        // Ruta + nombre del archivo.
        /*val routeWithoutFile = pathProvider.getCurrentPath().substringBeforeLast("/")
        val newPathFile = File("$routeWithoutFile/$newFileName")

        if (newPathFile.exists()){
            return FileAction.EXIST
        }

        val response = File(pathProvider.getCurrentPath()).renameTo(newPathFile)
        return if (response) FileAction.SUCCESS else FileAction.ERROR*/
    }
}
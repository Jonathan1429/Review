package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants.DATASTORE
import com.jonathanev.review.Core.Constants.GUIAS
import com.jonathanev.review.Core.Constants.IMAGENES
import com.jonathanev.review.Core.Constants.IMAGENESPIVOTE
import com.jonathanev.review.Core.Constants.PRINCIPAL
import com.jonathanev.review.Data.Model.prueba.UICreatingFile
import javax.inject.Inject

class ValidateCreateFileUseCase @Inject constructor() {
    operator fun invoke(name: String, description: String): UICreatingFile{
        val invalidChars = listOf("/", ".")
        val invalidNames = listOf(DATASTORE, GUIAS, IMAGENES, IMAGENESPIVOTE, PRINCIPAL)

        val message = when {
            name.isBlank() -> "Debes tener un nombre de archivo"

            invalidChars.any { char -> name.contains(char) } ->
                "No puede haber caracteres como / o . en el nombre"

            invalidNames.any { nameFile -> name.equals(nameFile, ignoreCase = true) } ->
                "Ese nombre no está permitido"

            else -> null
        }

        //val exist = fileExistUseCase.invoke(name)
        return if (!message.isNullOrEmpty()){
            UICreatingFile.Message(message)
        } else {
            UICreatingFile.ContinuedProcess(name, description)
        }
    }
}
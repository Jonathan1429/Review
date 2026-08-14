package com.jonathanev.review.domain

import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.domain.result.ValidateCreateFileResult
import com.jonathanev.review.presentation.model.FileFormMode
import javax.inject.Inject

class ValidateCreateFileUseCase @Inject constructor() {
    operator fun invoke(name: String, description: String, mode: FileFormMode): ValidateCreateFileResult {
        val cleanName = name.trim()
        val cleanDescription = description.trim()

        val invalidNames = listOf(
            StorageFolders.DATASTORE,
            StorageFolders.GUIAS,
            StorageFolders.IMAGENES,
            StorageFolders.IMAGENESPIVOTE,
            StorageFolders.PRINCIPAL
        )

        val validNameRegex = Regex("^[a-zA-Z0-9 áéíóúÁÉÍÓÚñÑ_-]+$")

        val message = when {
            cleanName.isBlank() -> {
                if (mode is FileFormMode.CreatingFolder || mode is FileFormMode.RenameFolder) {
                    "Debes tener un nombre de carpeta"
                } else {
                    "Debes tener un nombre de archivo"
                }
            }

            !cleanName.matches(validNameRegex) ->
                "Solo se permiten letras, números, espacios y guiones"

            invalidNames.any { nameFile -> cleanName.equals(nameFile, ignoreCase = true) } ->
                "Ese nombre no está permitido"

            else -> ""
        }

        return if (message.isNotEmpty()) {
            ValidateCreateFileResult.Error(message)
        } else {
            ValidateCreateFileResult.Success(cleanName, cleanDescription)
        }
    }
}
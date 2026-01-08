package com.jonathanev.review.domain

import com.jonathanev.review.presentation.state.CreatingFileUiState
import com.jonathanev.review.data.storage.StorageFolders
import javax.inject.Inject

class ValidateCreateFileUseCase @Inject constructor() {
    operator fun invoke(name: String, description: String): CreatingFileUiState {
        val invalidChars = listOf("/", ".")
        val invalidNames = listOf(
            StorageFolders.DATASTORE,
            StorageFolders.GUIAS,
            StorageFolders.IMAGENES,
            StorageFolders.IMAGENESPIVOTE,
            StorageFolders.PRINCIPAL
        )

        val message = when {
            name.isBlank() -> "Debes tener un nombre de archivo"

            invalidChars.any { char -> name.contains(char) } ->
                "No puede haber caracteres como / o . en el nombre"

            invalidNames.any { nameFile -> name.equals(nameFile, ignoreCase = true) } ->
                "Ese nombre no está permitido"

            else -> ""
        }

        return if (message.isNotEmpty()) {
            CreatingFileUiState.Message(message)
        } else {
            CreatingFileUiState.ContinuedProcess(name, description)
        }
    }
}
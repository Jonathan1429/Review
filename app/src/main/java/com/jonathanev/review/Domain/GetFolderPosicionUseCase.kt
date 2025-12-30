package com.jonathanev.review.Domain

import com.jonathanev.review.data.FolderResult
import com.jonathanev.review.data.Model.prueba.FolderUI
import javax.inject.Inject

class GetFolderPosicionUseCase @Inject constructor(){
    operator fun invoke(position: Int, folders: List<FolderUI>): FolderResult {
        val lista = folders//.value ?: return GuiaResult.Empty
        return lista.getOrNull(position)?.let { FolderResult.Success(it) }
            ?: FolderResult.Error("No se encontró la carpeta en la posición $position")
    }
}
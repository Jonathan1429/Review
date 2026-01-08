package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.folders.model.FolderResult
import javax.inject.Inject

class GetFolderPosicionUseCase @Inject constructor(){
    operator fun invoke(position: Int, folders: List<FolderDomainModel>): FolderResult {
        return folders.getOrNull(position)?.let {
            val folderToUi = it.toUi()
            FolderResult.Success(folderToUi)
        } ?: FolderResult.Error("No se encontró la carpeta en la posición $position")
    }
}
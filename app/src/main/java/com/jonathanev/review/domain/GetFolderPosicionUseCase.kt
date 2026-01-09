package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.folders.model.FolderResultDomain
import javax.inject.Inject

class GetFolderPosicionUseCase @Inject constructor(){
    operator fun invoke(position: Int, folders: List<FolderDomainModel>): FolderResultDomain {
        return folders.getOrNull(position)?.let {
            //val folderToUi = it.toUi()
            FolderResultDomain.Success(it)
        } ?: FolderResultDomain.Error("No se encontró la carpeta en la posición $position")
    }
}
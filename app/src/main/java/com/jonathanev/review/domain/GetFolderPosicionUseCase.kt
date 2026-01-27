package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.result.FolderResultDomain
import javax.inject.Inject

class GetFolderPosicionUseCase @Inject constructor(){
    operator fun invoke(position: Int, folders: List<FolderDomainModel>): FolderResultDomain {
        return folders.getOrNull(position)?.let {
            FolderResultDomain.Success(it)
        } ?: FolderResultDomain.Error("No se encontró la carpeta en la posición $position")
    }
}
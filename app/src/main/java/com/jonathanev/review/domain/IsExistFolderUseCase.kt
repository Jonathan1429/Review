package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.FolderRepository
import javax.inject.Inject

class IsExistFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    operator fun invoke(name: String): Boolean {
        val foldersDomain = folderRepository.getFolders()
        val folderDomainModel = foldersDomain.find { it.folder.name == name }
        return folderDomainModel != null
    }
}
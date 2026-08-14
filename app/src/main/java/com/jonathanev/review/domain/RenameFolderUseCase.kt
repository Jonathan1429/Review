package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.FolderScreenInfoDomain
import com.jonathanev.review.domain.repository.FolderRepository
import javax.inject.Inject

class RenameFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(
        oldName: String,
        newName: String,
        data: FolderScreenInfoDomain
    ): Boolean {
        return folderRepository.renameFolder(oldName, newName, data)
    }
}

package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.FolderScreenInfoDomain
import com.jonathanev.review.domain.repository.FolderRepository
import javax.inject.Inject

class CreateFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(data: FolderScreenInfoDomain): Boolean {
        return folderRepository.createFolder(data)
    }
}
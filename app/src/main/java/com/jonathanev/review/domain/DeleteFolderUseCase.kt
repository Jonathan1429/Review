package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.FolderRepository
import javax.inject.Inject

class DeleteFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(): Boolean {
        return folderRepository.deleteFolder()
    }
}
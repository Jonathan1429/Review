package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.FolderRepository
import javax.inject.Inject

class DeleteFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    operator fun invoke(nameFolder: String): Boolean {
        return folderRepository.deleteFolder(nameFolder)
    }
}
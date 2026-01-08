package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.FolderRepository
import javax.inject.Inject

class GetAllFoldersUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    operator fun invoke() {
        //return folderRepository.getFolders()
    }
}
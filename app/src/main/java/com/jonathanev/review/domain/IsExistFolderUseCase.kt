package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.FolderRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class IsExistFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(name: String): Boolean {
        val foldersDomain = folderRepository.getFolders().first()
        return foldersDomain.any { it.folder.name.equals(name, ignoreCase = true) }
    }
}
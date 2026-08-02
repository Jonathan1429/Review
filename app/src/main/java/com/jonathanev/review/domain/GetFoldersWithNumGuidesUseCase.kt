package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFoldersWithNumGuidesUseCase @Inject constructor(
    private val folderRepository: FolderRepository,
) {
    operator fun invoke(): Flow<List<FolderDomainModel>> {
        return folderRepository.getFolders()
    }
}
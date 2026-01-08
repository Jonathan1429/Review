package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.FolderWithNumGuidesDomainModel
import com.jonathanev.review.domain.repository.FolderRepository
import javax.inject.Inject

class GetFoldersWithNumGuidesUseCase @Inject constructor(
    private val folderRepository: FolderRepository,
) {
    operator fun invoke(): List<FolderWithNumGuidesDomainModel> {
        return folderRepository.getFolders()
    }
}
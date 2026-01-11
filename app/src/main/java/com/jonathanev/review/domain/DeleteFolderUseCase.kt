package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.FolderRepository
import com.jonathanev.review.presentation.event.UIStopEvent
import javax.inject.Inject

class DeleteFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    operator fun invoke(nameFolder: String): UIStopEvent {
        return folderRepository.deleteFolder(nameFolder)
    }
}
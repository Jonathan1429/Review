package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaMigrationRepository
import javax.inject.Inject

class InitializeGuideStorageUseCase @Inject constructor(
    private val directoryManager: DirectoryManager,
    private val migrationRepository: GuiaMigrationRepository
) {
    operator fun invoke(): Boolean {
        val foldersCreated = directoryManager.createFoldersMain()

        if (!foldersCreated){
            return false
        }

        migrationRepository.moveGuides()
        return true
    }
}
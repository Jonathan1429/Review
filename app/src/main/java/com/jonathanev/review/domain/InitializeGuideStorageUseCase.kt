package com.jonathanev.review.domain

import android.util.Log
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaMigrationRepository
import com.jonathanev.review.domain.repository.ImagesRepository
import javax.inject.Inject

class InitializeGuideStorageUseCase @Inject constructor(
    private val directoryManager: DirectoryManager,
    private val migrationRepository: GuiaMigrationRepository,
    private val imagesRepository: ImagesRepository
) {
    operator fun invoke(): Boolean {
        val foldersCreated = directoryManager.createFoldersMain()

        if (!foldersCreated){
            return false
        }

        val result = migrationRepository.moveGuides()
        if (!result.hasSuccess){
            return false
        }

        imagesRepository.moveUnassignedImages(result.movedFiles)

        /*if (result.failedFiles.isNotEmpty()) {
            Log.e("MIGRATION", "Fallaron archivos: ${result.failedFiles}")
        }*/
        return true
    }
}
package com.jonathanev.review.Domain

import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.prueba.FolderUI
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import java.io.File
import javax.inject.Inject

class GetFoldersWithNumGuidesUseCase @Inject constructor(
    private val fileRepositoryImpl: FileRepositoryImpl,
    private val filePathsProvider: FilePathsProvider,
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(): List<FolderUI> {
        val folders = guiaRepository.getFolders()
        val currentPath = File(fileRepositoryImpl.getCurrentPath())

        return folders.map { folder ->
            val currentFolder = filePathsProvider.buildFolder(currentPath, folder.nameFolder)
            val numGuidesCurrentFolder = currentFolder.listFiles()?.size ?: 0

            FolderUI(
                folderModel = folder,
                numGuides = numGuidesCurrentFolder
            )
        }
    }
}
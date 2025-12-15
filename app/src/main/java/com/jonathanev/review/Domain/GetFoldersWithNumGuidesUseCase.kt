package com.jonathanev.review.Domain

import com.jonathanev.review.Data.GuiaRepositoryImpl
import com.jonathanev.review.Data.Model.prueba.FolderUI
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class GetFoldersWithNumGuidesUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val filePathsProvider: FilePathsProvider,
    private val guiaRepositoryImpl: GuiaRepositoryImpl
) {
    operator fun invoke(): List<FolderUI> {
        val currentPath = File(fileRepository.getCurrentPath())
        val folders = guiaRepositoryImpl.getFolders(currentPath)

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
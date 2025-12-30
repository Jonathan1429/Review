package com.jonathanev.review.Domain

import com.jonathanev.review.data.GuiaRepositoryImpl
import com.jonathanev.review.data.Model.prueba.FolderModel
import com.jonathanev.review.data.Model.prueba.FolderUI
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.data.repository.FolderRepository
import com.jonathanev.review.Domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class GetFoldersWithNumGuidesUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val filePathsProvider: FilePathsProvider,
    private val folderRepository: FolderRepository,
    private val guiaRepositoryImpl: GuiaRepositoryImpl
) {
    operator fun invoke(): List<FolderUI> {
        val currentPath = File(fileRepository.getCurrentPath())
        val folders = guiaRepositoryImpl.getFolders(currentPath)

        return folders.map { folder ->
            val currentFolder = filePathsProvider.buildFolder(currentPath, folder.name)
            val attributes = folderRepository.getAttributesFolder(currentFolder)
            val numGuidesCurrentFolder = currentFolder.listFiles()?.filter { it.name != "screen.json" }?.size ?: 0

            FolderUI(
                folderModel = FolderModel(
                    name = attributes.name,
                    description = attributes.description,
                    imgFolder = attributes.imgFolder,
                    color = attributes.color
                ),
                numGuides = numGuidesCurrentFolder
            )
        }
    }
}
package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.model.FolderWithNumGuidesDomainModel
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.FolderRepository
import com.jonathanev.review.domain.repository.PathProvider
import java.io.File
import javax.inject.Inject

class GetFoldersWithNumGuidesUseCase @Inject constructor(
    private val pathProvider: PathProvider,
    private val filePathsProvider: FilePathsProvider,
    private val folderRepository: FolderRepository,
) {
    operator fun invoke(): List<FolderWithNumGuidesDomainModel> {
        val currentPath = File(pathProvider.getCurrentPath())
        val folders = folderRepository.getFolders()

        return folders.map { folder ->
            val currentFolder = filePathsProvider.buildFolder(currentPath, folder.name)
            val numGuidesCurrentFolder = currentFolder.listFiles()?.filter { it.name != "screen.json" }?.size ?: 0

            FolderWithNumGuidesDomainModel(
                folder = FolderDomainModel(folder.name),
                numGuides = numGuidesCurrentFolder
            )
        }
    }
}
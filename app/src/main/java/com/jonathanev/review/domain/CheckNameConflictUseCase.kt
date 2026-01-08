package com.jonathanev.review.domain

import com.jonathanev.review.presentation.folders.model.FolderAction
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.PathProvider
import java.io.File
import javax.inject.Inject

class CheckNameConflictUseCase @Inject constructor(
    private val pathProvider: PathProvider,
    private val filePathsProvider: FilePathsProvider,
    private val directoryManager: DirectoryManager
) {
    operator fun invoke(mode: FolderAction, name: String) {
        val basePath = File(pathProvider.getCurrentPath())

        val pathToCheck = when (mode) {
            FolderAction.CreatingFolder ->
                filePathsProvider.buildFolder(basePath, name)

            FolderAction.CreatingFile,
            FolderAction.RenamingFile ->
                filePathsProvider.buildFile(basePath, name)

            FolderAction.None -> error("FolderAction $mode no soportado")

            else -> return false
        }

        return directoryManager.existPath(pathToCheck.path)
    }
}
package com.jonathanev.review.Domain

import com.jonathanev.review.data.FolderAction
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.Domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class CheckNameConflictUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val filePathsProvider: FilePathsProvider,
    private val fileHelper: FileHelper
) {
    operator fun invoke(mode: FolderAction, name: String): Boolean {
        val basePath = File(fileRepository.getCurrentPath())

        val pathToCheck = when (mode) {
            FolderAction.CreatingFolder ->
                filePathsProvider.buildFolder(basePath, name)

            FolderAction.CreatingFile,
            FolderAction.RenamingFile ->
                filePathsProvider.buildFile(basePath, name)

            FolderAction.None -> error("FolderAction $mode no soportado")

            else -> return false
        }

        return fileHelper.exists(pathToCheck.path)
    }
}
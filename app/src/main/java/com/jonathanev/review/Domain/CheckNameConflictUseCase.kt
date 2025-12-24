package com.jonathanev.review.Domain

import com.jonathanev.review.Data.FolderAction
import com.jonathanev.review.Data.provider.FilePathsProvider
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
            FolderAction.CREATING_FOLDER ->
                filePathsProvider.buildFolder(basePath, name)

            FolderAction.CREATING_FILE,
            FolderAction.RENAMING_FILE ->
                filePathsProvider.buildFile(basePath, name)

            FolderAction.NONE -> error("FolderAction $mode no soportado")

            else -> return false
        }

        return fileHelper.exists(pathToCheck.path)
    }
}
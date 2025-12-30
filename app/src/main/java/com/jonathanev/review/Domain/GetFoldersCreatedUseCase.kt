package com.jonathanev.review.Domain

import com.jonathanev.review.Domain.repository.FileRepository
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.data.storage.StorageFolders
import java.io.File
import javax.inject.Inject

class GetFoldersCreatedUseCase @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val fileRepository: FileRepository
) {
    operator fun invoke(): Array<String>{
        val listaCarpetas = filePathsProvider.fileGuides.listFiles { file -> file.isDirectory } ?: emptyArray()
        val currentPath = fileRepository.getCurrentPath()
        val foldersCreated = listaCarpetas.map { it.name }.toTypedArray()

        if (File(currentPath) != filePathsProvider.fileGuides) {
            foldersCreated.plus(StorageFolders.PRINCIPAL)
        }

        return foldersCreated
    }
}
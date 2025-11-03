package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants.PRINCIPAL
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import java.io.File
import javax.inject.Inject

class GetFoldersCreatedUseCase @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val fileRepositoryImpl: FileRepositoryImpl
) {
    operator fun invoke(): Array<String>{
        val listaCarpetas = filePathsProvider.fileGuides.listFiles { file -> file.isDirectory } ?: emptyArray()
        val currentPath = fileRepositoryImpl.getCurrentPath()
        val foldersCreated = listaCarpetas.map { it.name }.toTypedArray()

        if (File(currentPath) != filePathsProvider.fileGuides) {
            foldersCreated.plus(PRINCIPAL)
        }

        return foldersCreated
    }
}
package com.jonathanev.review.Domain

import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.Domain.repository.FileNamingRules
import com.jonathanev.review.Domain.repository.FileRepository
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject

class MoverArchivoUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke(pathFile: File): Pair<Boolean, File> {
        val file = pathFile.path.substringAfterLast("/").replace(FileNamingRules.XML_EXTENSION, "")
        val currrentPath = File(fileRepository.getCurrentPath())
        val newPath = filePathsProvider.buildFile(currrentPath, file)

        Files.move(
            Paths.get(pathFile.path),
            Paths.get(newPath.path),
            StandardCopyOption.REPLACE_EXISTING
        )

       return Pair(newPath.exists(), newPath)
    }
}
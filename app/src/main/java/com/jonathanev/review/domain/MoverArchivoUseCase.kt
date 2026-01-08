package com.jonathanev.review.domain

import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.FileNamingRules
import com.jonathanev.review.domain.repository.PathProvider
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject

class MoverArchivoUseCase @Inject constructor(
    private val pathProvider: PathProvider,
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke(pathFile: File) {
        /*val file = pathFile.path.substringAfterLast("/").replace(FileNamingRules.XML_EXTENSION, "")
        val currrentPath = File(pathProvider.getCurrentPath())
        val newPath = filePathsProvider.buildFile(currrentPath, file)

        Files.move(
            Paths.get(pathFile.path),
            Paths.get(newPath.path),
            StandardCopyOption.REPLACE_EXISTING
        )

       return Pair(newPath.exists(), newPath)*/
    }
}
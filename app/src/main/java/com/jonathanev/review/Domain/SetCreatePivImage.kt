package com.jonathanev.review.Domain

import com.jonathanev.review.data.provider.FilePathsProvider
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject

class SetCreatePivImage @Inject constructor(
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke(originPath: File, copiedPath: File, noImage: String){
        Files.copy(
            Paths.get(originPath.toString()),
            Paths.get(copiedPath.toString()),
            StandardCopyOption.REPLACE_EXISTING
        )

        // Borrar archivo
        File(filePathsProvider.rutaPrin, noImage).delete()
    }
}
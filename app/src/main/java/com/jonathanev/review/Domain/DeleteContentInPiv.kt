package com.jonathanev.review.Domain

import com.jonathanev.review.Data.provider.FilePathsProvider
import java.io.File
import javax.inject.Inject

class DeleteContentInPiv @Inject constructor(
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke(nombreArchivo: String) {
        filePathsProvider.fileGuides
            .takeIf { it.exists() }
            ?.let { File(it, "$nombreArchivo.xml").delete() }


        val files = filePathsProvider.fileImagesPiv.listFiles()
        if (files != null) {
            for (subFile in files) {
                subFile.delete()
            }
        }
    }
}
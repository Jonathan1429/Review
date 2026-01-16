package com.jonathanev.review.data.filesystem

import android.content.Context
import com.jonathanev.review.domain.repository.FileNamingRules
import com.jonathanev.review.data.storage.StorageFolders
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilePathsProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val basePath: String = context.filesDir.path

    val fileGuides: String = "$basePath/${StorageFolders.GUIAS}"
    val fileImages: String = "$basePath/${StorageFolders.IMAGENES}"

    fun buildGuide(base: String, nombreArchivo: String): String {
        val file = FileNamingRules.buildXmlFileName(nombreArchivo)
        return "$base/$file"
    }

    fun buildImage(base: String, image: String): String {
        val file = FileNamingRules.buildPngFileName(image)
        return "$base/$file"
    }

    fun buildFolderGuide(base: String, folder: String, nombreArchivo: String): String {
        val file = FileNamingRules.buildXmlFileName(nombreArchivo)
        return "$base/$folder/$file"
    }

    fun buildFolder(base: String, folder: String): String {
        return "$base/$folder"
    }
}
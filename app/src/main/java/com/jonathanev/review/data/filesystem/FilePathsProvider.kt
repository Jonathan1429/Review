package com.jonathanev.review.data.filesystem

import android.content.Context
import com.jonathanev.review.data.storage.StorageFolders
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilePathsProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val basePath: String = context.filesDir.path

    val fileGuides: String = "$basePath/${StorageFolders.GUIAS}"
    val fileImages: String = "$basePath/${StorageFolders.IMAGENES}"

    fun buildGuide(base: String, file: String): String {
        return "$base/$file"
    }

    fun buildImage(base: String, image: String): String {
        return "$base/$image"
    }

    fun buildFolderGuide(base: String, folder: String, file: String): String {
        return "$base/$folder/$file"
    }

    fun buildFolder(base: String, folder: String): String {
        return "$base/$folder"
    }
}
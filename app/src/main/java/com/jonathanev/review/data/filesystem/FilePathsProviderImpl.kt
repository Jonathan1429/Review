package com.jonathanev.review.data.filesystem

import android.content.Context
import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.domain.provider.FilePathsProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilePathsProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
): FilePathsProvider {
    private val basePath: String = context.filesDir.path

    override val fileGuides: String
        get() = "$basePath/${StorageFolders.GUIAS}"
    override val fileImages: String
        get() = "$basePath/${StorageFolders.IMAGENES}"

    override fun buildGuide(base: String, file: String): String {
        return "$base/$file"
    }

    override fun buildImage(base: String, image: String): String {
        return "$base/$image"
    }

    override fun buildFolderGuide(base: String, folder: String, file: String): String {
        return "$base/$folder/$file"
    }

    override fun buildFolder(base: String, folder: String): String {
        return "$base/$folder"
    }
}
package com.jonathanev.review.data.filesystem

import android.content.Context
import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.domain.provider.FilePathsProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilePathsProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : FilePathsProvider {
    private val basePath: File = context.filesDir

    override val fileGuides: String
        get() = File(basePath, StorageFolders.GUIAS).path

    override val fileImages: String
        get() = File(basePath, StorageFolders.IMAGENES).path

    override fun buildGuide(base: String, file: String): String {
        return File(base, file).path
    }

    override fun buildImage(base: String, image: String): String {
        return File(base, image).path
    }

    override fun buildFolderGuide(base: String, folder: String, file: String): String {
        return if (folder.isBlank()) {
            File(base, file).path
        } else {
            File(File(base, folder), file).path
        }
    }

    override fun buildFolder(base: String, folder: String): String {
        return if (folder.isBlank()) {
            base
        } else {
            File(base, folder).path
        }
    }
}
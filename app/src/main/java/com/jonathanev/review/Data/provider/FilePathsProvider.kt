package com.jonathanev.review.Data.provider

import android.content.Context
import com.jonathanev.review.Core.Constants.GUIAS
import com.jonathanev.review.Core.Constants.IMAGENES
import com.jonathanev.review.Core.Constants.IMAGENESPIVOTE
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilePathsProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val basePath: String = context.filesDir.path
    private val basePathImgCifrado: String = "frqwhqw://phgld/slfnhu/"
    private val basePathImg: String = "content://media/picker/"

    val rutaPrinImgCifrado: File = File(basePathImgCifrado)
    val rutaPrinImg: File = File(basePathImg)
    val rutaPrin: File = File(basePath)
    val fileGuides: File = File("$basePath/$GUIAS")
    val fileImages: File = File("$basePath/$IMAGENES")
    val fileImagesPiv: File = File("$basePath/$IMAGENESPIVOTE")

    fun buildFile(base: File, nombreArchivo: String): File {
        return File("$base/$nombreArchivo")
    }

    fun buildFileFolder(base: File, folder: String, nombreArchivo: String): File {
        return File("$base/$folder/$nombreArchivo")
    }

    fun buildFolder(base: File, folder: String): File {
        return File("$base/$folder")
    }
}
package com.jonathanev.review.data.provider

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
    private val basePathImgCifrado: String = "frqwhqw://phgld/slfnhu/"
    private val basePathImg: String = "content://media/picker/"

    val rutaPrinImgCifrado: File = File(basePathImgCifrado)
    val rutaPrinImg: File = File(basePathImg)
    val rutaPrin: File = File(basePath)
    val fileGuides: File = File("$basePath/${StorageFolders.GUIAS}")
    val fileImages: File = File("$basePath/${StorageFolders.IMAGENES}")
    val fileImagesPiv: File = File("$basePath/${StorageFolders.IMAGENESPIVOTE}")

    fun buildGuide(base: File, nombreArchivo: String): File {
        val file = FileNamingRules.buildXmlFileName(nombreArchivo)
        return File("$base/$file")
    }

    fun buildImage(base: File, image: String): File {
        val file = FileNamingRules.buildPngFileName(image)
        return File("$base/$file")
    }

    fun buildFolderGuide(base: File, folder: String, nombreArchivo: String): File {
        val file = FileNamingRules.buildXmlFileName(nombreArchivo)
        return File("$base/$folder/$file")
    }

    fun buildFolder(base: File, folder: String): File {
        return File("$base/$folder")
    }

    /*fun buildImage(base: File, image: String): File{
        val pathImages = base.path.replace(StorageFolders.GUIAS, StorageFolders.IMAGENES)
        return File("$pathImages/$image")
    }*/

    fun beforePath(base: File): File {
        val beforePath = File(base.path.substringBeforeLast("/"))
        return beforePath
    }
}
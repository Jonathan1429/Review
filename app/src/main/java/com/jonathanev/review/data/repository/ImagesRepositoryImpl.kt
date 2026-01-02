package com.jonathanev.review.data.repository

import android.content.Context
import android.net.Uri
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.data.xml.Versions
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.PathProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject

class ImagesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pathProvider: PathProvider,
    private val filePathsProvider: FilePathsProvider
) : ImagesRepository {
    override suspend fun saveImage(image: QuestionContentDomain.Image, imagesPath: File) {
        val uri = Uri.parse(image.uri)
        val fileName = image.nameFile

        val outputFile = File(imagesPath, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("No se pudo abrir imagen")
    }

    override fun reubicarImagenes(
        version: String,
        fileName: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>
    ) {
        val currentPath = File(pathProvider.getCurrentPath())
        val oldPathImages = File(currentPath.path.replace(StorageFolders.GUIAS, StorageFolders.IMAGENES).replace(".xml", ""))

        if (version == Versions.VERSION2){
            val newPathImages = File(oldPathImages.parent, fileName)
            oldPathImages.renameTo(newPathImages)
        } else { // Version 1
            val newPathImages = File(oldPathImages.parent, fileName)
            newPathImages.mkdir()

            val listImages = (preguntas + respuestas)
                .flatMap { it.content }
                .filterIsInstance<QuestionContentDomain.Image>()

            listImages.forEach { image ->
                val source = File(image.uri)
                if (source.exists()){
                    val noImage = image.uri.substringAfterLast("/")
                    val newPathFile = filePathsProvider.buildImage(newPathImages, noImage)

                    Files.copy(
                        Paths.get(image.uri),
                        Paths.get(newPathFile.path),
                        StandardCopyOption.REPLACE_EXISTING
                    )

                    // Delete old Image
                    if (newPathFile.exists()){
                        File(image.uri).delete()
                    }
                }
            }
        }
    }
}
package com.jonathanev.review.data.repository

import android.content.Context
import android.net.Uri
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.data.xml.Versions
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject

class ImagesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val filePathsProvider: FilePathsProvider,
    private val navigationPathRepository: NavigationPathRepository
) : ImagesRepository {
    override suspend fun saveImage(image: QuestionContentDomain.Image, nameFolder: String) {
        val currentPath =
            filePathsProvider.buildImage(navigationPathRepository.currentPath, nameFolder)
        val uri = Uri.parse(image.uri)
        val fileName = image.nameFile
        val outputFile = File(currentPath, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("No se pudo abrir imagen")
    }

    override fun reubicarImagenes(
        fileName: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        attributesGuide: GuideDomainModel
    ) {
        val oldPathImages = filePathsProvider.buildImage(navigationPathRepository.currentPath, fileName)

        // Renamed folder
        if (attributesGuide.version == Versions.VERSION2){
            val newPathImages = File(oldPathImages.parent, fileName)
            oldPathImages.renameTo(newPathImages)
        } else { // Version 1
            val newPathImages = File(oldPathImages.parent, fileName)

            if (newPathImages.exists()){
                newPathImages.mkdir()
            }

            val listImages = (preguntas + respuestas)
                .flatMap { it.content }
                .filterIsInstance<QuestionContentDomain.Image>()

            listImages.forEach { image ->
                val source = File(image.uri)
                if (source.exists()){
                    val noImage = image.uri.substringAfterLast("/")
                    val newPathFile = filePathsProvider.buildImage(newPathImages, noImage)

                    Files.move(
                        Paths.get(image.uri),
                        Paths.get(newPathFile.path),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
            }
        }
    }
}
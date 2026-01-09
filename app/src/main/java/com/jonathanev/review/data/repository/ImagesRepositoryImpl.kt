package com.jonathanev.review.data.repository

import android.content.Context
import android.net.Uri
import com.jonathanev.review.data.provider.FilePathsProvider
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
        /*val currentPath =
            filePathsProvider.buildImage(navigationPathRepository.currentPathGuides, nameFolder)*/
        val currentPath =
            filePathsProvider.buildFolder(navigationPathRepository.currentPathImages, nameFolder)
        val uri = Uri.parse(image.uri)
        val fileName = image.nameFile
        val outputFile = File(currentPath, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("No se pudo abrir imagen")
    }

    override fun deleteImages(
        guideDomainModel: GuideDomainModel,
        listImages: List<QuestionContentDomain.Image>
    ) {

        if (guideDomainModel.version == Versions.VERSION1) {
            val basePathImages = navigationPathRepository.currentPathImages

            //filePathsProvider.buildFolder(navigationPathRepository.currentPathImages, guideDomainModel.nameGuide)
            listImages.forEach { image ->
                val noImage = File(image.uri.substringAfterLast("/")).nameWithoutExtension
                val currentPath = filePathsProvider.buildImage(basePathImages, noImage)
                if (currentPath.exists()) {
                    currentPath.delete()
                }
            }
        } else {
            val basePathImages = filePathsProvider.buildFolder(
                navigationPathRepository.currentPathImages,
                guideDomainModel.nameGuide
            )

            basePathImages.deleteRecursively()
        }
    }

    override fun reubicarImagenes(
        fileName: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        attributesGuide: GuideDomainModel
    ) {
        /*val oldPathImages = filePathsProvider.buildImage(navigationPathRepository.currentPathGuides, fileName)*/
        val oldPathImages =
            filePathsProvider.buildFolder(navigationPathRepository.currentPathImages, fileName)

        // Renamed folder
        if (attributesGuide.version == Versions.VERSION2) {
            val newPathImages = File(oldPathImages.parent, fileName)
            oldPathImages.renameTo(newPathImages)
        } else { // Version 1
            val newPathImages = File(oldPathImages.parent, fileName)

            if (newPathImages.exists()) {
                newPathImages.mkdir()
            }

            val listImages = (preguntas + respuestas)
                .flatMap { it.content }
                .filterIsInstance<QuestionContentDomain.Image>()

            listImages.forEach { image ->
                val source = File(image.uri)
                if (source.exists()) {
                    val noImage = File(image.uri.substringAfterLast("/")).nameWithoutExtension
                    val newPathImage = filePathsProvider.buildImage(newPathImages, noImage)

                    Files.move(
                        Paths.get(image.uri),
                        Paths.get(newPathImage.path),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
            }
        }
    }
}
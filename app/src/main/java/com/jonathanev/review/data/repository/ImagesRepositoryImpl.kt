package com.jonathanev.review.data.repository

import android.content.Context
import android.net.Uri
import com.jonathanev.review.data.filesystem.Extensions
import com.jonathanev.review.data.filesystem.FilePathsProvider
import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.presentation.navigation.NavigationPathRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class ImagesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val filePathsProvider: FilePathsProvider,
    private val navigationPathRepository: NavigationPathRepository
) : ImagesRepository {
    override suspend fun save(image: QuestionContentDomain.Image, nameFolder: String) {
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

    override fun movingImagesToOtros() {
        val currentPathImages = File(navigationPathRepository.currentPathImages)
        val images = currentPathImages.listFiles()
            ?.filter { it.isFile && it.extension == Extensions.PNG_EXTENSION}
            ?: emptyList()

        if (images.isNotEmpty()){
            val pathImageOtros = File(navigationPathRepository.currentPathImages, StorageFolders.OTROS)

            if (!pathImageOtros.exists()) {
                pathImageOtros.mkdir()
            }

            // Mover cada archivo imagen
            images.forEach { file ->
                val newPath = File(pathImageOtros, file.name)
                file.renameTo(newPath)
            }
        }
    }

    override fun delete(
        guideDomainModel: GuideDomainModel,
        listImages: List<QuestionContentDomain.Image>
    ): Boolean {

        if (guideDomainModel.version == GuideVersion.V1) {
            val basePathImages = navigationPathRepository.currentPathImages

            listImages.forEach { image ->
                val noImage = File(image.uri.substringAfterLast("/")).nameWithoutExtension
                val currentPath = File(filePathsProvider.buildImage(basePathImages, noImage))
                if (currentPath.exists()) {
                    currentPath.delete()
                }
            }

            return true
        } else {
            val basePathImages = filePathsProvider.buildFolder(
                navigationPathRepository.currentPathImages,
                guideDomainModel.nameGuide
            )

            return File(basePathImages).deleteRecursively()
        }
    }

    override fun reubicarImagenes(
        fileName: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        attributesGuide: GuideDomainModel
    ): Boolean {
        val oldPathImages =
            getSourceImagePath(navigationPathRepository.currentPathImages, attributesGuide)

        // Renamed folder
        if (attributesGuide.version == GuideVersion.V2) {
            val newPathImages = File(filePathsProvider.buildFolder(navigationPathRepository.currentPathImages, fileName))
            return File(oldPathImages).renameTo(newPathImages)
        } else { // Version 1 a Version 2
            val newPathImages = File(oldPathImages, fileName)

            if (!newPathImages.exists()) {
                newPathImages.mkdir()
            }

            val listImages = (preguntas + respuestas)
                .flatMap { it.content }
                .filterIsInstance<QuestionContentDomain.Image>()

            var isSuccess = true
            listImages.forEach { image ->
                val source = File(image.uri)
                if (source.exists()) {
                    val noImage = File(image.uri.substringAfterLast("/")).nameWithoutExtension
                    val newPathImage = filePathsProvider.buildImage(newPathImages.path, noImage)

                    val isRenamed = File(image.uri).renameTo(File(newPathImage))
                    if (!isRenamed) isSuccess = false
                }
            }

            return isSuccess
        }
    }

    private fun getSourceImagePath(
        sourceImagePath: String,
        guideDomain: GuideDomainModel,
    ): String {
        return if (guideDomain.version == GuideVersion.V1) {
            sourceImagePath
        } else {
            filePathsProvider.buildFolder(
                sourceImagePath,
                guideDomain.nameGuide
            )
        }
    }
}
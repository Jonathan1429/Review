package com.jonathanev.review.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.domain.constants.Extensions
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideRenameContext
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.service.FileNamingRules
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class ImagesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val filePathsProvider: FilePathsProvider,
    private val navigationPathRepository: NavigationPathRepository
) : ImagesRepository {
    override fun save(image: QuestionContentDomain.Image, guide: GuideDomainModel) {
        val currentPath =
            filePathsProvider.buildFolder(navigationPathRepository.getPathImages().value, guide.nameGuide)
        val uri = Uri.parse(image.uri)
        val fileName = image.nameFile
        val outputFile = File(currentPath, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("No se pudo abrir imagen")
    }

    override fun moveUnassignedImages(movedFiles: List<String>) {
        val currentPathImages = File(context.filesDir, StorageFolders.IMAGENES)
        val images = currentPathImages.listFiles()
            ?.filter { it.isFile && it.extension == Extensions.PNG_EXTENSION }
            ?: emptyList()

        if (images.isNotEmpty()){
            val pathImageOtros = File(currentPathImages, StorageFolders.OTROS)

            val isFolderReady = pathImageOtros.exists() || pathImageOtros.mkdirs()

            if (!isFolderReady) {
                Log.e("MIGRATION", "No se pudo preparar la carpeta de destino Otros.")
                return
            }

            // Mover cada archivo imagen
            images.forEach { file ->
                val newPath = File(pathImageOtros, file.name)
                file.renameTo(newPath)
            }
        }
    }

    override fun deleteImages(
        guide: GuideDomainModel,
        images: List<QuestionContentDomain.Image>
    ): Boolean {

        if (guide.version == GuideVersion.V1) {
            val basePathImages = navigationPathRepository.getPathImages().value

            images.forEach { image ->
                val noImage = File(image.uri.substringAfterLast("/")).nameWithoutExtension
                val imagePng = FileNamingRules.buildPngFileName(noImage)
                val currentPath = File(filePathsProvider.buildImage(basePathImages, imagePng))

                if (currentPath.exists()) {
                    currentPath.delete()
                }
            }

            return true
        } else {
            val basePathImages = filePathsProvider.buildFolder(
                navigationPathRepository.getPathImages().value,
                guide.nameGuide
            )

            return File(basePathImages).deleteRecursively()
        }
    }

    override fun moveImages(
        images: List<QuestionContentDomain.Image>,
        guideRenameContext: GuideRenameContext
    ): Boolean {
        val oldPathImages =
            getSourceImagePath(navigationPathRepository.getPathImages().value, guideRenameContext.oldGuide)

        // Renamed folder
        if (guideRenameContext.oldGuide.version == GuideVersion.V2) {
            val newPathImages = File(filePathsProvider.buildFolder(navigationPathRepository.getPathImages().value, guideRenameContext.newName))
            return File(oldPathImages).renameTo(newPathImages)
        } else { // Version 1 a Version 2
            val newPathImages = File(oldPathImages, guideRenameContext.newName)

            if (!newPathImages.exists()) {
                newPathImages.mkdir()
            }

            var isSuccess = true
            images.forEach { image ->
                val source = File(image.uri)
                if (source.exists()) {
                    val noImage = File(image.uri.substringAfterLast("/")).nameWithoutExtension
                    val imagePng = FileNamingRules.buildPngFileName(noImage)
                    val newPathImage = filePathsProvider.buildImage(newPathImages.path, imagePng)

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
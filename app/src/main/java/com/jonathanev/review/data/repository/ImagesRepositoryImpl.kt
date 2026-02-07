package com.jonathanev.review.data.repository

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.domain.constants.Extensions
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideRenameContext
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.service.FileNamingRules
import com.jonathanev.review.domain.service.FilePathResolverService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class ImagesRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val filePathsProvider: FilePathsProvider,
    private val filePathResolverService: FilePathResolverService
) : ImagesRepository {
    override fun save(
        image: QuestionContentDomain.Image,
        guide: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath
    ) {
        val relativeGuidePath =
            filePathResolverService.mapToJoinRelativePath(relativeGuidePath, guide.nameGuide)
        val currentPath =
            filePathResolverService.mapToFolderPath(relativeGuidePath, PathKind.IMAGENES).value
        val uri = image.uri.toUri()
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

        if (images.isNotEmpty()) {
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
        images: List<QuestionContentDomain.Image>,
        relativeGuidePath: RelativeGuidePath
    ): Boolean {

        if (guide.version == GuideVersion.V1) {
            val relativeGuidePath =
                filePathResolverService.mapToJoinRelativePath(relativeGuidePath, guide.nameGuide)
            val basePathImages = filePathResolverService.mapToFolderPath(
                relativeGuidePath,
                PathKind.IMAGENES
            ).value

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
            val relativeGuidePath =
                filePathResolverService.mapToJoinRelativePath(relativeGuidePath, guide.nameGuide)
            val basePathImages = File(
                filePathResolverService.mapToFolderPath(
                    relativeGuidePath,
                    PathKind.IMAGENES
                ).value
            )

            return basePathImages.deleteRecursively()
        }
    }

    override fun moveImages(
        images: List<QuestionContentDomain.Image>,
        guideRenameContext: GuideRenameContext,
        relativeGuidePath: RelativeGuidePath
    ): Boolean {
        val oldRelativeGuidePath = filePathResolverService.mapToJoinRelativePath(
            relativeGuidePath,
            guideRenameContext.oldGuide.nameGuide
        )
        val oldPathImages = File(
            filePathResolverService.mapToFolderPath(
                oldRelativeGuidePath,
                PathKind.IMAGENES
            ).value
        )

        // Renamed folder
        if (guideRenameContext.oldGuide.version == GuideVersion.V2) {
            val relativeGuidePath = filePathResolverService.mapToJoinRelativePath(
                relativeGuidePath,
                guideRenameContext.newName
            )
            val newPathImages = File(
                filePathResolverService.mapToFolderPath(
                    relativeGuidePath,
                    PathKind.IMAGENES
                ).value
            )
            return oldPathImages.renameTo(newPathImages)
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
}
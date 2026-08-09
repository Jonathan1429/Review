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
import com.jonathanev.review.domain.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.FilePathResolver
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.service.FileNamingRules
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject

class ImagesRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val filePathsProvider: FilePathsProvider,
    private val filePathResolver: FilePathResolver
) : ImagesRepository {
    override suspend fun save(
        image: QuestionContentDomain.Image,
        guide: GuideDomainModel
    ): Unit = withContext(Dispatchers.IO) {
        val currentPath = File(
            filePathResolver.mapToFolderPathSpecificGuide(
                guideDomainModel = guide,
                kind = PathKind.IMAGENES
            ).value
        )

        // Asegurar que el directorio de imágenes exista antes de escribir
        if (!currentPath.exists()) {
            currentPath.mkdirs()
        }

        val uri = image.uri.toUri()
        val outputFile = File(currentPath, image.nameFile)

        // Manejo seguro de streams
        context.contentResolver.openInputStream(uri)?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("No se pudo abrir el stream de la imagen")
    }

    override suspend fun saveTempImage(uriString: String): String = withContext(Dispatchers.IO) {
        val uri = uriString.toUri()
        val tempFile = File(context.cacheDir, "temp_img_${System.currentTimeMillis()}.jpg")

        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("No se pudo leer la imagen desde: $uri")

        tempFile.toUri().toString()
    }

    override suspend fun clearTempImages() {
        withContext(Dispatchers.IO) {
            val cacheFolder = context.cacheDir
            val files = cacheFolder.listFiles() ?: return@withContext

            files.forEach { file ->
                if (file.name.startsWith("temp_img_")) {
                    file.delete()
                }
            }
        }
    }

    override suspend fun moveUnassignedImages(
        movedFiles: List<String>
    ) = withContext(Dispatchers.IO) {
        val currentPathImages = File(context.filesDir, StorageFolders.IMAGENES)

        if (!currentPathImages.exists() || !currentPathImages.isDirectory) return@withContext

        val movedFilesSet = movedFiles.toSet()

        val unassignedImages = currentPathImages.listFiles()?.filter { file ->
            file.isFile &&
                    file.extension.equals(Extensions.PNG_EXTENSION, ignoreCase = true) &&
                    file.name !in movedFilesSet
        } ?: emptyList()

        if (unassignedImages.isEmpty()) return@withContext

        val pathImageOtros = File(currentPathImages, StorageFolders.OTROS)
        if (!pathImageOtros.exists() && !pathImageOtros.mkdirs()) {
            Log.e("MIGRATION", "No se pudo preparar la carpeta de destino Otros.")
            return@withContext
        }

        unassignedImages.forEach { file ->
            val targetFile = File(pathImageOtros, file.name)
            try {
                Files.move(
                    file.toPath(),
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            } catch (e: Exception) {
                Log.e("MIGRATION", "Error al mover archivo no asignado: ${file.name}", e)
            }
        }
    }

    override suspend fun deleteImages(
        guide: GuideDomainModel,
        images: List<QuestionContentDomain.Image>,
    ): Boolean {

        if (guide.version == GuideVersion.V1) {
            val basePathImages =
                filePathResolver.mapToFolderPathSpecificGuide(guide, PathKind.IMAGENES).value

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
            val basePathImages =
                File(filePathResolver.mapToFolderPathSpecificGuide(guide, PathKind.IMAGENES).value)

            return basePathImages.deleteRecursively()
        }
    }

    override suspend fun moveImages(
        images: List<QuestionContentDomain.Image>,
        guideRenameContext: GuideRenameContext
    ): Boolean {
        val oldFolderImages =
            File(
                filePathResolver.mapToFolderPathSpecificGuide(
                    guideDomainModel = guideRenameContext.oldGuide,
                    kind = PathKind.IMAGENES
                ).value
            )

        val newPathImages =
            File(
                filePathResolver.mapToFolderPathSpecificGuide(
                    guideDomainModel = guideRenameContext.newGuide,
                    kind = PathKind.IMAGENES
                ).value
            )

        // Renamed folder
        if (guideRenameContext.oldGuide.version == GuideVersion.V2) {
            if (!newPathImages.exists()) {
                newPathImages.mkdir()
            }

            var isSuccess = true
            images.forEach { image ->
                val source = File(image.uri)
                if (source.exists()) {
                    val noImage = File(image.uri.substringAfterLast("/")).nameWithoutExtension
                    val imagePng = FileNamingRules.buildPngFileName(noImage)
                    val oldPathImage = filePathsProvider.buildImage(oldFolderImages.path, imagePng)
                    val newPathImage = filePathsProvider.buildImage(newPathImages.path, imagePng)

                    val isRenamed = File(oldPathImage).renameTo(File(newPathImage))
                    if (!isRenamed) isSuccess = false
                }
            }

            return isSuccess
        } else { // Version 1 a Version 2
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
package com.jonathanev.review.data.filesystem

import com.jonathanev.review.domain.constants.Extensions
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.ImageContext
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.FilePathResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject

class DirectoryManagerImpl @Inject constructor(
    private val filePathResolver: FilePathResolver,
    private val filePathsProvider: FilePathsProvider,
) : DirectoryManager {
    override suspend fun createPathImages(
        guideDomainModel: GuideDomainModel,
        isNewFile: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val currentPath = File(
            filePathResolver.mapToFolderPathSpecificGuide(
                guideDomainModel = guideDomainModel,
                kind = PathKind.IMAGENES
            ).value
        )

        if (isNewFile && currentPath.exists()) {
            currentPath.deleteRecursively()
        }

        if (!currentPath.exists()) {
            currentPath.mkdirs()
        } else {
            true
        }
    }

    override fun existPath(path: String): Boolean {
        return File(path).exists()
    }

    override suspend fun moveImages(
        guideDomainModel: GuideDomainModel,
        imageContext: ImageContext,
        images: List<QuestionContentDomain.Image>
    ): Boolean = withContext(Dispatchers.IO) {
        val (oldImagesPath, newImagesPath) = when (imageContext) {
            is ImageContext.MovingImage -> {
                val old = filePathResolver.mapToOldFolderPath(
                    guideDomainModel = guideDomainModel,
                    originContext = imageContext,
                    kind = PathKind.IMAGENES
                )
                val new = filePathResolver.mapToFolderPathSpecificGuide(
                    guideDomainModel = guideDomainModel,
                    kind = PathKind.IMAGENES
                )
                Pair(old, new)
            }

            is ImageContext.Save -> {
                val new = filePathResolver.mapToFolderPathSpecificGuide(
                    guideDomainModel = GuideDomainModel(
                        version = GuideVersion.V2,
                        nameGuide = guideDomainModel.nameGuide,
                        description = guideDomainModel.description
                    ),
                    kind = PathKind.IMAGENES
                )
                Pair(new, new)
            }
        }

        if (oldImagesPath.value == newImagesPath.value) {
            return@withContext true
        }

        val targetDir = File(newImagesPath.value)
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            return@withContext false
        }

        var isSuccess = true

        images.forEach { image ->
            if (image.nameFile.isBlank()) return@forEach

            val oldPathImage = File(oldImagesPath.value, image.nameFile)

            if (oldPathImage.exists()) {
                val newPathImage = File(targetDir, image.nameFile)
                try {
                    Files.move(
                        oldPathImage.toPath(),
                        newPathImage.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                } catch (_: Exception) {
                    isSuccess = false
                }
            }
        }

        val oldDir = File(oldImagesPath.value)
        if (oldDir.isDirectory && oldDir.listFiles()?.isEmpty() == true) {
            oldDir.delete()
        }

        isSuccess
    }

    override suspend fun deleteLeftoverImagesInDevice(
        guideDomainModel: GuideDomainModel,
        listImages: List<QuestionContentDomain.Image>
    ) = withContext(Dispatchers.IO) {
        val currentPath = File(
            filePathResolver.mapToFolderPathSpecificGuide(
                guideDomainModel = guideDomainModel,
                kind = PathKind.IMAGENES
            ).value
        )

        if (!currentPath.exists() || !currentPath.isDirectory) return@withContext

        val validImageNames = listImages
            .mapNotNull { image -> image.nameFile.takeIf { it.isNotBlank() } }
            .toSet()

        currentPath.listFiles()?.forEach { file ->
            if (file.isFile && file.name !in validImageNames) {
                file.delete()
            }
        }
    }

    override suspend fun createPathGuide(
        guideDomainModel: GuideDomainModel
    ): Boolean = withContext(Dispatchers.IO) {
        val currentPath = filePathResolver.mapToFolderPath(
            kind = PathKind.GUIAS
        )

        val folder = File(currentPath.value)

        if (folder.exists()) {
            true
        } else {
            folder.mkdirs()
        }
    }

    override fun createFoldersMain(): Boolean {
        val paths = listOf(
            File(filePathsProvider.fileGuides),
            File(filePathsProvider.fileImages),
        )

        for (path in paths) {
            if (!path.exists()) {
                path.mkdir()
            }
        }

        return !(!paths[0].exists() || !paths[1].exists())
    }

    override fun deleteFolderEmpty(context: GuideContext.Moving) {
        val pathGuides = File(
            filePathResolver.mapToOldFolderPath(
                guideDomainModel = context.guide,
                originContext = context,
                kind = PathKind.GUIAS
            ).value
        )

        val pathImages = File(
            filePathResolver.mapToOldFolderPath(
                guideDomainModel = context.guide,
                originContext = context,
                kind = PathKind.IMAGENES
            ).value
        )

        deleteIfEmpty(pathGuides)
        deleteIfEmpty(pathImages)
    }

    private fun deleteIfEmpty(folder: File) {
        if (folder.isDirectory && folder.listFiles()?.isEmpty() == true) {
            folder.delete()
        }
    }

    override suspend fun getImagesInDevice(
        guideDomain: GuideDomainModel
    ): Set<String> = withContext(Dispatchers.IO) {
        val currentPath = filePathResolver.mapToFolderPathSpecificGuide(
            guideDomainModel = guideDomain,
            kind = PathKind.IMAGENES
        )

        val folder = File(currentPath.value)

        if (!folder.exists() || !folder.isDirectory) {
            return@withContext emptySet()
        }

        folder.listFiles()
            ?.filter { file ->
                file.isFile && file.extension.equals(Extensions.PNG_EXTENSION, ignoreCase = true)
            }
            ?.mapTo(mutableSetOf()) { it.name }
            ?: emptySet()
    }
}
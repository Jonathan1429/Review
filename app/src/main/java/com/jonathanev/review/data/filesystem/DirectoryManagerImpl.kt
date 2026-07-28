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
import java.io.File
import javax.inject.Inject

class DirectoryManagerImpl @Inject constructor(
    private val filePathResolver: FilePathResolver,
    private val filePathsProvider: FilePathsProvider,
) : DirectoryManager {
    override suspend fun createPathImages(
        guideDomainModel: GuideDomainModel,
        isNewFile: Boolean
    ): Boolean {
        val currentPath =
            File(
                filePathResolver.mapToFolderPathSpecificGuide(
                    guideDomainModel = guideDomainModel,
                    kind = PathKind.IMAGENES
                ).value
            )

        when {
            isNewFile -> {
                if (currentPath.exists()) {
                    currentPath.deleteRecursively()
                }
                return currentPath.mkdir()
            }

            else -> {
                if (!currentPath.exists()) {
                    return currentPath.mkdir()
                }
                return true
            }
        }
    }

    override fun existPath(path: String): Boolean {
        return File(path).exists()
    }

    override suspend fun moveImages(
        guideDomainModel: GuideDomainModel,
        imageContext: ImageContext,
        images: List<QuestionContentDomain.Image>
    ): Boolean {
        val (oldImagesPath, newImagesPath) = when (imageContext) {
            is ImageContext.MovingImage -> {
                val old = filePathResolver.mapToOldFolderPathSpecificGuide(
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

        var isSuccess = true

        images.forEach { image ->
            val oldPathImage = File(oldImagesPath.value, image.nameFile)

            if (oldPathImage.exists()) {
                val newPathImages = File(newImagesPath.value, image.nameFile)
                val successImage = oldPathImage.renameTo(newPathImages)
                if (!successImage) isSuccess = false
            }
        }

        return isSuccess
    }

    override suspend fun deleteLeftoverImagesInDevice(
        guideDomainModel: GuideDomainModel,
        listImages: List<QuestionContentDomain.Image>
    ) {
        val currentPath =
            File(
                filePathResolver.mapToFolderPathSpecificGuide(
                    guideDomainModel,
                    PathKind.IMAGENES
                ).value
            )
        // Borrar imagenes que ya no estén en el XML pero si en el dispositivo
        val currentDeviceNames =
            currentPath.listFiles()?.map { it.name }?.toSet() ?: emptySet()
        val listDelete = currentDeviceNames - listImages.map { it.nameFile }.toSet()

        listDelete.forEach { image ->
            val destination = File(currentPath, image)
            if (destination.exists() && destination.isFile) {
                destination.delete()
            }
        }
    }

    override suspend fun createPathGuide(guideDomainModel: GuideDomainModel): Boolean {
        val currentPath =
            filePathResolver.mapToFolderPathSpecificGuide(guideDomainModel, PathKind.GUIAS)

        File(currentPath.value).mkdir()
        return File(currentPath.value).exists()
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
        val pathGuides =
            File(
                filePathResolver.mapToOldFolderPathSpecificGuide(
                    guideDomainModel = context.guide,
                    originContext = context,
                    kind = PathKind.GUIAS
                ).value
            )

        val pathImages =
            File(
                filePathResolver.mapToOldFolderPathSpecificGuide(
                    guideDomainModel = context.guide,
                    originContext = context,
                    kind = PathKind.IMAGENES
                ).value
            )

        if (pathGuides.delete()) {
            pathImages.delete()
        }
    }

    override suspend fun getImagesInDevice(
        guideDomain: GuideDomainModel
    ): Set<String> {
        val currentPath =
            filePathResolver.mapToFolderPathSpecificGuide(
                guideDomain,
                PathKind.IMAGENES
            )

        return File(currentPath.value).listFiles()
            ?.filter { it.isFile && it.extension == Extensions.PNG_EXTENSION }
            ?.map { it.name }
            ?.toSet() ?: emptySet()
    }
}
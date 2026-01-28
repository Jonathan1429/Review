package com.jonathanev.review.data.filesystem

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.ImageSource
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.service.FilePathResolverService
import java.io.File
import javax.inject.Inject

class DirectoryManagerImpl @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository,
    private val filePathResolverService: FilePathResolverService,
    private val filePathsProvider: FilePathsProvider,
) : DirectoryManager {
    override fun createPathImages(guideDomainModel: GuideDomainModel, isNewFile: Boolean): Boolean {
        val currentPath =
            File(
                filePathsProvider.buildFolder(
                    navigationPathRepository.getPathImages().value,
                    guideDomainModel.nameGuide
                )
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

    override fun moveImages(
        guideDomain: GuideDomainModel,
        imageSource: ImageSource,
        images: List<QuestionContentDomain.Image>
    ): Boolean {
        val pathImages = when (imageSource) {
            is ImageSource.MovingGuide -> imageSource.oldPath
            is ImageSource.SaveGuide -> imageSource.currentPath
        }

        val oldImagesPath = filePathResolverService.mapToSourceImagePath(
            GuideContext.Moving(
                guideDomain,
                pathImages,
                pathImages
            )
        )

        var isSuccess = true

        images.forEach { image ->
            val oldPathImage = File(getPathImage(guideDomain, image, oldImagesPath))
            val newPathImages = filePathResolverService.mapToSourceImagePath(
                GuideContext.Moving(
                    guideDomain,
                    pathImages,
                    navigationPathRepository.getPathImages()
                )
            )

            if (oldPathImage.exists()) {
                val destination = File(newPathImages, image.nameFile)
                val successImage = oldPathImage.renameTo(destination)
                if (!successImage) isSuccess = false
            }
        }

        return isSuccess
    }

    override fun deleteLeftoverImagesInDevice(
        nameGuide: String,
        listImages: List<QuestionContentDomain.Image>
    ) {
        val currentPath =
            filePathsProvider.buildFolder(navigationPathRepository.getPathImages().value, nameGuide)
        // Borrar imagenes que ya no estén en el XML pero si en el dispositivo
        val currentDeviceNames =
            File(currentPath).listFiles()?.map { it.name }?.toSet() ?: emptySet()
        val listDelete = currentDeviceNames - listImages.map { it.nameFile }.toSet()

        listDelete.forEach { image ->
            val destination = File(currentPath, image)
            if (destination.exists() && destination.isFile) {
                destination.delete()
            }
        }
    }

    override fun createPathGuide(nameGuide: String): Boolean {
        val currentPath =
            File(
                filePathsProvider.buildFolder(
                    navigationPathRepository.getPathGuides().value,
                    nameGuide
                )
            )

        currentPath.mkdir()
        return currentPath.exists()
    }

    override fun prepareGuidePath(newName: String): Boolean {
        val currentPath =
            File(
                filePathsProvider.buildFolder(
                    navigationPathRepository.getPathGuides().value,
                    newName
                )
            )

        currentPath.mkdir()
        return currentPath.exists()
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

    override fun deleteFolderEmpty(context: GuideContext.Moving): Boolean {
        return if (context.guide.version == GuideVersion.V2) {
            File(context.oldGuidePath.value, context.guide.nameGuide).delete()
        } else {
            true
        }
    }

    override fun getImagesInDevice(guideDomain: GuideDomainModel): Set<String> {
        val currentPath =
            filePathsProvider.buildFolder(
                navigationPathRepository.getPathImages().value,
                guideDomain.nameGuide
            )

        return File(currentPath).listFiles()?.map { it.name }?.toSet() ?: emptySet()
    }

    private fun getPathImage(
        guideDomain: GuideDomainModel,
        image: QuestionContentDomain.Image,
        oldImagesPath: String
    ): String {
        return if (guideDomain.version == GuideVersion.V1) {
            val nameFile = image.uri.substringAfterLast("/")
            "$oldImagesPath/$nameFile"
        } else {
            "$oldImagesPath/${image.nameFile}"
        }
    }
}
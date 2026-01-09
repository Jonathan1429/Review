package com.jonathanev.review.data.provider

import com.jonathanev.review.domain.DirectoryManager
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.repository.NavigationPathRepository
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject

class DirectoryManagerImpl @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository,
    private val filePathsProvider: FilePathsProvider,
) : DirectoryManager {
    override fun prepareCleanDirectory(guideDomainModel: GuideDomainModel, isNewFile: Boolean) {
        /*val currentPath = filePathsProvider.buildImage(
            navigationPathRepository.currentPathGuides,
            guideDomainModel.nameGuide
        )*/

        val currentPath = filePathsProvider.buildFolder(
            navigationPathRepository.currentPathImages,
            guideDomainModel.nameGuide
        )
        when {
            isNewFile -> {
                if (currentPath.exists()) {
                    currentPath.deleteRecursively()
                }
                currentPath.mkdir()
            }

            else -> {
                if (!currentPath.exists()) {
                    currentPath.mkdir()
                }
            }
        }
    }

    override fun existPath(path: String): Boolean {
        return File(path).exists()
    }

    override suspend fun moveImagesV1(
        listImages: List<QuestionContentDomain.Image>,
        nameGuide: String
    ) {
        /*val currentPath =
            filePathsProvider.buildImage(navigationPathRepository.currentPathGuides, nameGuide)*/
        val currentPath =
            filePathsProvider.buildFolder(navigationPathRepository.currentPathImages, nameGuide)

        listImages.forEach { image ->
            if (File(image.uri).exists()) {
                val destination = File(currentPath, image.nameFile)
                Files.move(
                    Paths.get(image.uri),
                    Paths.get(destination.path),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        }
    }

    override fun deleteLeftoverImagesInDevice(
        nameGuide: String,
        listImages: List<QuestionContentDomain.Image>
    ) {
        /*val currentPath =
            filePathsProvider.buildImage(navigationPathRepository.currentPathGuides, nameGuide)*/
        val currentPath =
            filePathsProvider.buildFolder(navigationPathRepository.currentPathImages, nameGuide)
        // Borrar imagenes que ya no estén en el XML pero si en el dispositivo
        val currentDeviceNames = currentPath.listFiles()?.map { it.name }?.toSet() ?: emptySet()
        val listDelete = currentDeviceNames - listImages.map { it.nameFile }.toSet()

        listDelete.forEach { image ->
            val destination = File(currentPath, image)
            if (destination.exists() && destination.isFile) {
                destination.delete()
            }
        }
    }

    override fun createPathGuide(nameGuide: String) {
        val currentPath =
            filePathsProvider.buildFolder(navigationPathRepository.currentPathGuides, nameGuide)
        if (!currentPath.exists()) {
            currentPath.mkdir()
        }
    }
}
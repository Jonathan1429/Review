package com.jonathanev.review.domain

import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.NavigationPathRepository
import java.io.File
import javax.inject.Inject

class UpdateImagesUseCase @Inject constructor(
    private val directoryManager: DirectoryManager,
    private val saveGuideImagesUseCase: SaveGuideImagesUseCase,
    private val filePathsProvider: FilePathsProvider,
    private val navigationPathRepository: NavigationPathRepository
) {
    suspend operator fun invoke(
        guide: GuideDomainModel,
        preguntasProcesadas: List<QuestionItemDomain>,
        respuestasProcesadas: List<QuestionItemDomain>,
        isNewFile: Boolean,
    ) {
        // Preparar la carpeta para las imagenes.
        directoryManager.createPathImages(guide, isNewFile)

        val listImages = (preguntasProcesadas + respuestasProcesadas)
            .flatMap { it.content }
            .filterIsInstance<QuestionContentDomain.Image>()

        if (isNewFile) {
            if (listImages.isNotEmpty()) {
                saveGuideImagesUseCase.saveImagesInDevice(listImages, guide.nameGuide)
            }
        } else {
            directoryManager.moveImages(
                listImages = listImages,
                nameGuide = guide.nameGuide,
                version = guide.version
            )

            // Add new Images.
            val currentPath =
                filePathsProvider.buildFolder(
                    navigationPathRepository.currentPathGuides,
                    guide.nameGuide
                )

            val currentDeviceNames =
                currentPath.listFiles()?.map { it.name }?.toSet() ?: emptySet()

            val addImages =
                listImages.filter { it.nameFile !in currentDeviceNames && it.uri.isNotEmpty() }
            if (addImages.isNotEmpty()) {
                saveGuideImagesUseCase.saveImagesInDevice(addImages, guide.nameGuide)
            }
            // Delete images V2
            directoryManager.deleteLeftoverImagesInDevice(guide.nameGuide, listImages)

        }
    }
}
package com.jonathanev.review.domain

import com.jonathanev.review.data.Extensions
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.data.xml.Versions
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.repository.PathProvider
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
        directoryManager.prepareCleanDirectory(guide, isNewFile)

        val listImages = (preguntasProcesadas + respuestasProcesadas)
            .flatMap { it.content }
            .filterIsInstance<QuestionContentDomain.Image>()

        if (isNewFile) {
            if (listImages.isNotEmpty()) {
                saveGuideImagesUseCase.saveImagesInDevice(listImages, guide.nameGuide)
            }
        } else {
            if (guide.version == Versions.VERSION1) {
                directoryManager.moveImagesV1(
                    listImages = listImages,
                    nameGuide = guide.nameGuide
                )
            }

            // Add new Images.
            val currentPath =
                filePathsProvider.buildImage(navigationPathRepository.currentPath, guide.nameGuide)
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
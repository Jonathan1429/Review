package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.presentation.navigation.NavigationPathRepository
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.repository.DirectoryManager
import javax.inject.Inject

class UpdateImagesUseCase @Inject constructor(
    private val directoryManager: DirectoryManager,
    private val saveGuideImagesUseCase: SaveGuideImagesUseCase,
    private val navigationPathRepository: NavigationPathRepository
) {
    suspend operator fun invoke(
        guideDomain: GuideDomainModel,
        preguntasProcesadas: List<QuestionItemDomain>,
        respuestasProcesadas: List<QuestionItemDomain>,
        isNewFile: Boolean,
    ): Boolean {
        // Preparar la carpeta para las imagenes.
        directoryManager.createPathImages(guideDomain, isNewFile)
        val listImages = (preguntasProcesadas + respuestasProcesadas)
            .flatMap { it.content }
            .filterIsInstance<QuestionContentDomain.Image>()

        if (!isNewFile) {
            val isSuccessMoveImages =
                directoryManager.moveImages(
                    guideDomain,
                    ImageSource.SaveGuide(GuidePath(navigationPathRepository.currentPathImages)),
                    listImages
                )
            if (!isSuccessMoveImages) return false
        }

        val imagesInDevice = directoryManager.getImagesInDevice(guideDomain)

        val addImages =
            listImages.filter { it.nameFile !in imagesInDevice && it.uri.isNotEmpty() }

        if (addImages.isNotEmpty()) {
            saveGuideImagesUseCase.saveImagesInDevice(addImages, guideDomain.nameGuide)
        }

        // Borrar imagenes que se encuentren en el dispositivo y no en el archivo
        directoryManager.deleteLeftoverImagesInDevice(guideDomain.nameGuide, listImages)

        return true
    }
}
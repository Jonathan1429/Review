package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.ImageSource
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class UpdateImagesUseCase @Inject constructor(
    private val directoryManager: DirectoryManager,
    private val imagesRepository: ImagesRepository,
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(
        guideDomain: GuideDomainModel,
        preguntasProcesadas: List<QuestionItemDomain>,
        respuestasProcesadas: List<QuestionItemDomain>,
        isNewFile: Boolean,
    ): Boolean {
        // Preparar la carpeta para las imagenes.
        val pathImages = directoryManager.createPathImages(guideDomain, isNewFile)
        if (!pathImages) return false

        val listImages = (preguntasProcesadas + respuestasProcesadas)
            .flatMap { it.content }
            .filterIsInstance<QuestionContentDomain.Image>()

        if (!isNewFile) {
            val isSuccessMoveImages =
                directoryManager.moveImages(
                    guideDomain,
                    ImageSource.SaveGuide(navigationPathRepository.getPathImages()),
                    listImages
                )
            if (!isSuccessMoveImages) return false
        }

        val imagesInDevice = directoryManager.getImagesInDevice(guideDomain)

        val addImages =
            listImages.filter { it.nameFile !in imagesInDevice && it.uri.isNotEmpty() }

        addImages.forEach { image ->
            imagesRepository.save(image, guideDomain)
        }

        // Borrar imagenes que se encuentren en el dispositivo y no en el archivo
        directoryManager.deleteLeftoverImagesInDevice(guideDomain.nameGuide, listImages)

        return true
    }
}
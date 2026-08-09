package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.ImageContext
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.SaveGuideMode
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.ImagesRepository
import javax.inject.Inject

class UpdateImagesUseCase @Inject constructor(
    private val directoryManager: DirectoryManager,
    private val imagesRepository: ImagesRepository
) {
    suspend operator fun invoke(
        guideDomain: GuideDomainModel,
        preguntasProcesadas: List<QuestionItemDomain>,
        respuestasProcesadas: List<QuestionItemDomain>,
        saveGuideMode: SaveGuideMode
    ): Boolean {
        val isNewFile = saveGuideMode == SaveGuideMode.Create

        // Preparar la carpeta para las imagenes.
        val pathImages = directoryManager.createPathImages(
            guideDomainModel = GuideDomainModel(
                version = GuideVersion.V2,
                nameGuide = guideDomain.nameGuide,
                description = guideDomain.description
            ),
            isNewFile = isNewFile
        )
        if (!pathImages) return false

        val listImages = (preguntasProcesadas + respuestasProcesadas)
            .flatMap { it.content }
            .filterIsInstance<QuestionContentDomain.Image>()

        if (!isNewFile) {
            val isSuccessMoveImages =
                directoryManager.moveImages(
                    guideDomainModel = guideDomain,
                    //imageContext = ImageContext.Update,
                    imageContext = ImageContext.Save,
                    images = listImages
                )
            if (!isSuccessMoveImages) return false
        }

        val imagesInDevice = directoryManager.getImagesInDevice(
            GuideDomainModel(
                version = GuideVersion.V2,
                nameGuide = guideDomain.nameGuide,
                description = guideDomain.description
            )
        )

        val addImages =
            listImages.filter { it.nameFile !in imagesInDevice && it.uri.isNotEmpty() }

        addImages.forEach { image ->
            imagesRepository.save(image, guideDomain)
        }

        // Borrar imagenes que se encuentren en el dispositivo y no en el archivo
        directoryManager.deleteLeftoverImagesInDevice(
            guideDomain,
            listImages
        )

        // Borra imagenes de la ruta cache
        imagesRepository.clearTempImages()

        return true
    }
}
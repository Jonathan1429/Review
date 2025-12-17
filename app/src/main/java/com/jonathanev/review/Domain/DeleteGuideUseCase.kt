package com.jonathanev.review.Domain

import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.UIStopEvent
import java.io.File
import javax.inject.Inject

class DeleteGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val deleteImagesFromGuideUseCase: DeleteImagesFromGuideUseCase
) {
    operator fun invoke(currentGuide: File, listImages: List<QuestionContent.Image>): UIStopEvent {
        val version = guiaRepository.getVersion(currentGuide)
        val isDeleteImages = deleteImagesFromGuideUseCase.invoke(version, currentGuide, listImages)

        return if (currentGuide.delete() && isDeleteImages){
            UIStopEvent.DeleteGuideSuccess("Guia eliminada correctamente")
        } else {
            UIStopEvent.ShowMessage("No se pudo eliminar la guia")
        }
    }
}
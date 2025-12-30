package com.jonathanev.review.Domain

import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.presentation.model.QuestionContent
import com.jonathanev.review.presentation.event.UIStopEvent
import java.io.File
import javax.inject.Inject

class DeleteGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val deleteImagesFromGuideUseCase: DeleteImagesFromGuideUseCase
) {
    operator fun invoke(currentGuide: File, listImages: List<QuestionContent.Image>): UIStopEvent {
        val version = guiaRepository.getVersion(currentGuide)

        return if (currentGuide.delete()){
            deleteImagesFromGuideUseCase.invoke(version, currentGuide, listImages)
            UIStopEvent.DeleteGuideSuccess("Guia eliminada correctamente")
        } else {
            UIStopEvent.ShowMessage("No se pudo eliminar la guia")
        }
    }
}
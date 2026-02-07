package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.model.SaveGuideMode
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.GetSaveGuideResult
import com.jonathanev.review.domain.result.UpdateGuideResult
import javax.inject.Inject

class SetCrearXmlUseCase @Inject constructor(
    private val setDecodePathImageUseCase: SetDecodePathImageUseCase,
    private val loadGuidesUseCase: LoadGuidesUseCase,
    private val setLabelsUseCase: SetLabelsUseCase,
    private val updateImagesUseCase: UpdateImagesUseCase,
    private val directoryManager: DirectoryManager,
    private val guiaRepository: GuiaRepository
) {
    suspend operator fun invoke(
        nameGuide: String,
        description: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        relativeGuidePath: RelativeGuidePath,
        mode: SaveGuideMode
    ): UpdateGuideResult {
        val (preguntasProcesadas, respuestasProcesadas) = setDecodePathImageUseCase.invoke(
            preguntas,
            respuestas
        )

        val guides = loadGuidesUseCase.invoke(relativeGuidePath)
        val guide = guides.find { it.nameGuide == nameGuide }
        if (guide == null) {
            return UpdateGuideResult.ErrorUpdateGuide
        }

        val (dataWithTagsQ, dataWithTagsA) =
            setLabelsUseCase.invoke(preguntasProcesadas, respuestasProcesadas)

        val path = directoryManager.createPathGuide(relativeGuidePath, nameGuide)
        if (!path) {
            return UpdateGuideResult.ErrorPath
        }

        val resultGuide = guiaRepository.saveGuide(
            guideDomainModel = GuideDomainModel(guide.version, nameGuide, description),
            preguntas = dataWithTagsQ,
            respuestas = dataWithTagsA,
            relativeGuidePath = relativeGuidePath
        )

        if (resultGuide is GetSaveGuideResult.Failure) {
            return UpdateGuideResult.SaveFailed(resultGuide.error)
        }

        val imagesUpdated = updateImagesUseCase.invoke(
            guideDomain = guide,
            preguntasProcesadas = preguntasProcesadas,
            respuestasProcesadas = respuestasProcesadas,
            isNewFile = mode != SaveGuideMode.Update,
            relativeGuidePath = relativeGuidePath
        )
        return if (imagesUpdated) {
            UpdateGuideResult.Success
        } else {
            UpdateGuideResult.ImagesFailed
        }
    }
}
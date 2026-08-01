package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.SaveGuideMode
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.GuideResource
import com.jonathanev.review.domain.result.UpdateGuideResult
import kotlinx.coroutines.flow.first
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
        guideDomainModel: GuideDomainModel,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        saveGuideMode: SaveGuideMode
    ): UpdateGuideResult {
        val (preguntasProcesadas, respuestasProcesadas) = setDecodePathImageUseCase.invoke(
            preguntas,
            respuestas
        )

        val guidesList = loadGuidesUseCase().first()

        val version = guidesList
            .find { it.nameGuide == guideDomainModel.nameGuide }
            ?.version

        if (saveGuideMode == SaveGuideMode.Update && version == null) {
            return UpdateGuideResult.ErrorUpdateGuide
        }

        if (saveGuideMode == SaveGuideMode.Update) {
            val guide = guidesList.find { it.nameGuide == guideDomainModel.nameGuide }

            if (guide == null) {
                return UpdateGuideResult.ErrorUpdateGuide
            }
        }

        val (dataWithTagsQ, dataWithTagsA) =
            setLabelsUseCase.invoke(preguntasProcesadas, respuestasProcesadas)

        val path = directoryManager.createPathGuide(guideDomainModel)
        if (!path) {
            return UpdateGuideResult.ErrorPath
        }

        val resultGuide = guiaRepository.saveGuide(
            guideDomainModel = guideDomainModel,
            preguntas = dataWithTagsQ,
            respuestas = dataWithTagsA
        )

        if (resultGuide is GuideResource.Error) {
            return UpdateGuideResult.SaveFailed(resultGuide.exception)
        }

        val imagesUpdated = updateImagesUseCase.invoke(
            guideDomain = GuideDomainModel(
                version ?: GuideVersion.V2,
                guideDomainModel.nameGuide,
                guideDomainModel.description
            ),
            preguntasProcesadas = preguntasProcesadas,
            respuestasProcesadas = respuestasProcesadas,
            saveGuideMode = saveGuideMode
        )
        return if (imagesUpdated) {
            UpdateGuideResult.Success
        } else {
            UpdateGuideResult.ImagesFailed
        }
    }
}
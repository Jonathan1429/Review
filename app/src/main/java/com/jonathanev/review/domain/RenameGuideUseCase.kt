package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideRenameContext
import com.jonathanev.review.domain.model.RequiredAttrGuide
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.RenamedGuideResult
import com.jonathanev.review.domain.mapper.GuideQuestionExtractor
import com.jonathanev.review.domain.model.OptionalAttrGuide
import com.jonathanev.review.domain.model.RelativeGuidePath
import javax.inject.Inject

class RenameGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val guideQuestionExtractor: GuideQuestionExtractor,
    private val imagesRepository: ImagesRepository,
    private val directoryManager: DirectoryManager,
) {
    operator fun invoke(
        guide: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath,
        newName: String,
        description: String
    ): RenamedGuideResult {
        return when (val result = guiaRepository.getXMLGuide(guide, relativeGuidePath)) {
            is GetGuideResult.Success -> {
                val (questions, answers) = guideQuestionExtractor.map(result)

                val isPathExist = directoryManager.prepareGuidePath(newName)
                if (!isPathExist) {
                    //navigationPathRepository.reset()
                    return RenamedGuideResult.GuidePathError
                }

                val isRenamed = guiaRepository.renameGuide(
                    preguntas = questions,
                    respuestas = answers,
                    guideContext = GuideContext.Rename(
                        guide = guide,
                        relativeGuidePath = relativeGuidePath,
                        name = RequiredAttrGuide(newName),
                        description = OptionalAttrGuide(description)
                    )
                )

                if (!isRenamed) {
                    //navigationPathRepository.reset()
                    return RenamedGuideResult.RenamedError
                }

                val images = extractImages(questions, answers)

                val isSuccess = imagesRepository.moveImages(
                    images,
                    GuideRenameContext(result.guideDomainModel, newName)
                )

                //navigationPathRepository.reset()
                if (!isSuccess) {
                    return RenamedGuideResult.ImageError
                }

                return RenamedGuideResult.Success
            }

            GetGuideResult.NotFound -> RenamedGuideResult.NotFound
            GetGuideResult.InvalidFormat -> RenamedGuideResult.InvalidFormat
            GetGuideResult.Error -> RenamedGuideResult.Error
            GetGuideResult.UnknownError -> RenamedGuideResult.UnknownError
        }
    }

    private fun extractImages(
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>
    ): List<QuestionContentDomain.Image> {
        return (preguntas + respuestas)
            .flatMap { it.content }
            .filterIsInstance<QuestionContentDomain.Image>()
    }
}
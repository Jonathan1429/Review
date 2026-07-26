package com.jonathanev.review.domain

import com.jonathanev.review.domain.mapper.GuideQuestionExtractor
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideRenameContext
import com.jonathanev.review.domain.model.OptionalAttrGuide
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RequiredAttrGuide
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.GuideResource
import com.jonathanev.review.domain.result.RenamedGuideResult
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RenameGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val guideQuestionExtractor: GuideQuestionExtractor,
    private val imagesRepository: ImagesRepository,
    private val directoryManager: DirectoryManager,
    private val navigationPathRepository: NavigationPathRepository
) {
    suspend operator fun invoke(
        guide: GuideDomainModel,
        newName: String,
        newDescription: String
    ): RenamedGuideResult {
        val relativeGuidePath = navigationPathRepository.relativePath.first()

        return when (val result = guiaRepository.getXMLGuide(guide)) {
            is GetGuideResult.Success -> {
                val (questions, answers) = guideQuestionExtractor.map(result)

                val isPathExist = directoryManager.createPathGuide(relativeGuidePath, newName)
                if (!isPathExist) {
                    return RenamedGuideResult.GuidePathError
                }

                val isRenamed = guiaRepository.renameGuide(
                    preguntas = questions,
                    respuestas = answers,
                    guideContext = GuideContext.Rename(
                        guide = guide,
                        relativeGuidePath = relativeGuidePath,
                        name = RequiredAttrGuide(newName),
                        description = OptionalAttrGuide(newDescription)
                    )
                )

                if (isRenamed is GuideResource.Error) {
                    return RenamedGuideResult.RenamedError
                }

                val images = extractImages(questions, answers)

                val isSuccess = imagesRepository.moveImages(
                    images,
                    GuideRenameContext(result.guideDomainModel, newName),
                    relativeGuidePath
                )

                //navigationPathRepository.reset()
                if (!isSuccess) {
                    return RenamedGuideResult.ImageError
                }


                RenamedGuideResult.Success
            }

            GetGuideResult.NotFound -> RenamedGuideResult.NotFound
            GetGuideResult.InvalidFormat -> RenamedGuideResult.InvalidFormat
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
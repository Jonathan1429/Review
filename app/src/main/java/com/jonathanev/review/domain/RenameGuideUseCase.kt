package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideRenameContext
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.PathResolve
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.RenamedGuideResult
import com.jonathanev.review.presentation.mapper.GuidePreviewMapper
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.service.FileNamingRules
import javax.inject.Inject

class RenameGuideUseCase @Inject constructor(
    private val obtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val guiaRepository: GuiaRepository,
    private val guidePreviewMapper: GuidePreviewMapper,
    private val imagesRepository: ImagesRepository,
    private val directoryManager: DirectoryManager,
    private val pathResolve: PathResolve,
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(
        guide: GuideDomainModel,
        newName: String,
        description: String
    ): RenamedGuideResult {
        val oldFile = FileNamingRules.buildXmlFileName(guide.nameGuide)
        val newFile = FileNamingRules.buildXmlFileName(guide.nameGuide)
        val context = GuideContext.Rename(
            guide = guide,
            currentGuidePath = pathResolve.currentPathResolve(guide, oldFile),
            newGuidePath = pathResolve.newPathResolve(newName, newFile)
        )

        return when (val result = obtenerDatosXMLUseCase.invoke(context)) {
            is GetGuideResult.Success -> {
                val (questions, answers) = guidePreviewMapper.map(result)

                val isPathExist = directoryManager.prepareGuidePath(newName)
                if (!isPathExist) {
                    return RenamedGuideResult.GuidePathError
                }

                val isRenamed = guiaRepository.renameGuide(
                    newName,
                    description,
                    questions,
                    answers,
                    context
                )

                if (!isRenamed) {
                    return RenamedGuideResult.RenamedError
                }

                val images = extractImages(questions, answers)

                val isSuccess = imagesRepository.moveImages(
                    images,
                    GuideRenameContext(result.guideDomainModel, newName)
                )

                if (!isSuccess) {
                    return RenamedGuideResult.ImageError
                }

                navigationPathRepository.reset()
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
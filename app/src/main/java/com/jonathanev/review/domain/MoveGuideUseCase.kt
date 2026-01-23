package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.ImageSource
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.MoveGuideResponse
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.service.FileNamingRules
import javax.inject.Inject

class MoveGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val directoryManager: DirectoryManager,
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(guideData: GetGuideResult.Success, context: GuideContext.Moving): MoveGuideResponse {
        var isExistPathGuide = true

        if (context.guide.version == GuideVersion.V2) {
            isExistPathGuide = directoryManager.createPathGuide(context.guide.nameGuide)
        }

        if (!isExistPathGuide) {
            return MoveGuideResponse.ErrorPathGuide
        }

        val file = FileNamingRules.buildXmlFileName(context.guide.nameGuide)
        val moveGuide = guiaRepository.moveGuide(
            file,
            GuideContext.Moving(
                context.guide,
                GuidePath(context.oldGuidePath.value),
                GuidePath(navigationPathRepository.currentPathGuides.value),
                GuidePath(context.oldImagePath.value),
            )
        )
        if (!moveGuide) {
            return MoveGuideResponse.ErrorMovingGuide
        }

        val isDeleteFolder = directoryManager.deleteFolderEmpty(
            GuideContext.Actual(
                guide = context.guide,
                currentGuidePath = GuidePath(context.oldGuidePath.value),
            )
        )

        if (!isDeleteFolder) {
            return MoveGuideResponse.WarningDeleteFolder
        }

        var isSuccessFolderImages =  true

        if (context.guide.version == GuideVersion.V2) {
            isSuccessFolderImages = directoryManager.createPathImages(
                guideDomainModel = context.guide,
                isNewFile = true
            )
        }

        if (!isSuccessFolderImages) {
            return MoveGuideResponse.ErrorPathImages
        }

        val images = extractImagesFromData(guideData.list)

        val isSuccessMoveImages =
            directoryManager.moveImages(
                context.guide,
                ImageSource.MovingGuide(GuidePath(context.oldImagePath.value)),
                images
            )

        return if(isSuccessMoveImages) MoveGuideResponse.Success else MoveGuideResponse.ErrorMovingImages
    }

    private fun extractImagesFromData(data: List<QAItemDomain>): List<QuestionContentDomain.Image> {
        // Esta lógica de filtrado SÍ puede estar aquí porque usa modelos de Dominio
        return data.flatMap { listOf(it.question, it.answer) }
            .filterIsInstance<ResponseDomain.Filled>()
            .flatMap { it.item.content }
            .filterIsInstance<QuestionContentDomain.Image>()
    }
}
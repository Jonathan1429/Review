package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.ImageContext
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.MoveGuideResponse
import javax.inject.Inject

class MoveGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val directoryManager: DirectoryManager
) {
    suspend operator fun invoke(
        guideData: GetGuideResult.Success,
        context: GuideContext.Moving
    ): MoveGuideResponse {
        var isExistPathGuide = true

        if (context.guide.version == GuideVersion.V2) {
            isExistPathGuide = directoryManager.createPathGuide(
                context.guide
            )
        }

        if (!isExistPathGuide) {
            return MoveGuideResponse.ErrorPathGuide
        }

        val moveGuide = guiaRepository.moveGuide(context)
        if (!moveGuide) {
            return MoveGuideResponse.ErrorMovingGuide
        }

        var isSuccessFolderImages = true

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
                guideDomainModel = context.guide,
                imageContext = ImageContext.MovingImage(
                    context.oldRelativeGuidePath
                ),
                images = images
            )

        directoryManager.deleteFolderEmpty(context)
        return if (isSuccessMoveImages) MoveGuideResponse.Success else MoveGuideResponse.ErrorMovingImages
    }

    private fun extractImagesFromData(data: List<QAItemDomain>): List<QuestionContentDomain.Image> {
        // Esta lógica de filtrado SÍ puede estar aquí porque usa modelos de Dominio
        return data.flatMap { listOf(it.question, it.answer) }
            .flatMap { it.content }
            .filterIsInstance<QuestionContentDomain.Image>()
    }
}
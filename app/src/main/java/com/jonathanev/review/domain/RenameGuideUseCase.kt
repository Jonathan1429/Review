package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.RenamedGuideResult
import com.jonathanev.review.presentation.navigation.NavigationPathRepository
import javax.inject.Inject

class RenameGuideUseCase @Inject constructor(
    private val uploadContentUseCase: UploadContentUseCase,
    private val guiaRepository: GuiaRepository,
    private val navigationPathRepository: NavigationPathRepository,
    private val imagesRepository: ImagesRepository
) {
    operator fun invoke(
        fileName: String,
        description: String,
        result: GetGuideResult.Success
    ): RenamedGuideResult {
        val (questions, answers) = uploadContentUseCase.invoke(result)
        val isRenamed = guiaRepository.renameGuide(
            fileName,
            description,
            questions,
            answers,
            GuideContext.Actual(
                result.guideDomainModel,
                GuidePath(navigationPathRepository.currentPathGuides)
            ),
        )
        if (!isRenamed) {
            return RenamedGuideResult.RenamedError
        }
        val isSuccess = imagesRepository.reubicarImagenes(fileName, questions, answers, result.guideDomainModel)
            //reubicarImagenesUseCase.invoke(fileName, questions, answers, result.guideDomainModel)


        if (!isSuccess) {
            return RenamedGuideResult.ImageError
        }

        navigationPathRepository.reset()
        return RenamedGuideResult.Success
    }
}
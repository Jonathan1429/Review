package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.data.repository.NavigationPathRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.RenamedGuideResult
import javax.inject.Inject

class RenameGuideUseCase @Inject constructor(
    private val uploadContentUseCase: UploadContentUseCase,
    private val reubicarImagenesUseCase: ReubicarImagenesUseCase,
    private val guiaRepository: GuiaRepository,
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(
        fileName: String,
        description: String,
        result: GetGuideResult.Success
    ): RenamedGuideResult {
        val (questions, answers) = uploadContentUseCase.invoke(result)
        val isRenamed = guiaRepository.renameGuide(fileName, description, questions, answers, result.guideDomainModel, navigationPathRepository.currentPathGuides)
        if (!isRenamed){
            return RenamedGuideResult.RenamedError
        }
        val isSuccess = reubicarImagenesUseCase.invoke(fileName, questions, answers, result.guideDomainModel)
        if (!isSuccess){
            return RenamedGuideResult.ImageError
        }

        return RenamedGuideResult.Sucess
    }
}
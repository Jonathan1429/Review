package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.PathResolve
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.RenamedGuideResult
import com.jonathanev.review.presentation.navigation.NavigationPathRepository
import javax.inject.Inject

class RenameGuideUseCase @Inject constructor(
    private val obtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val guiaRepository: GuiaRepository,
    private val uploadContentUseCase: UploadContentUseCase,
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
        val context = GuideContext.Rename(
            guide = guide,
            currentGuidePath = pathResolve.currentPathResolve(guide),
            newGuidePath = pathResolve.newPathResolve(newName)
        )

        return when (val result = obtenerDatosXMLUseCase.invoke(context)) {
            is GetGuideResult.Success -> {
                val (questions, answers) = uploadContentUseCase.invoke(result)

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
                val isSuccess = imagesRepository.reubicarImagenes(
                    newName,
                    questions,
                    answers,
                    result.guideDomainModel
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
}
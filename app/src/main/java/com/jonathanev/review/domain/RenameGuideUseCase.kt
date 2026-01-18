package com.jonathanev.review.domain

import com.jonathanev.review.data.filesystem.FilePathsProvider
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.RenamedGuideResult
import com.jonathanev.review.presentation.navigation.NavigationPathRepository
import javax.inject.Inject

class RenameGuideUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository,
    private val obtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val guiaRepository: GuiaRepository,
    private val uploadContentUseCase: UploadContentUseCase,
    private val imagesRepository: ImagesRepository,
    private val filePathsProvider: FilePathsProvider,
    private val directoryManager: DirectoryManager
) {
    operator fun invoke(
        guide: GuideDomainModel,
        newName: String,
        description: String
    ): RenamedGuideResult {
        val context = GuideContext.Rename(
            guide = guide,
            currentGuidePath = GuidePath(prepareGuidePath(guide)),
            newGuidePath = GuidePath(
                filePathsProvider.buildFolderGuide(
                    navigationPathRepository.currentPathGuides,
                    newName,
                    newName
                )
            )
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

    private fun prepareGuidePath(guideDomainModel: GuideDomainModel): String {
        return if (guideDomainModel.version == GuideVersion.V1) {
            filePathsProvider.buildGuide(
                navigationPathRepository.currentPathGuides,
                guideDomainModel.nameGuide
            )
        } else {
            filePathsProvider.buildFolderGuide(
                navigationPathRepository.currentPathGuides,
                guideDomainModel.nameGuide,
                guideDomainModel.nameGuide
            )
        }
    }
}
/*class RenameGuideUseCase @Inject constructor(
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
}*/
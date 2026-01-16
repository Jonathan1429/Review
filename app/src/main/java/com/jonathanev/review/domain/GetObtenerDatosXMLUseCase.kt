package com.jonathanev.review.domain

import com.jonathanev.review.data.filesystem.FilePathsProvider
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.data.repository.NavigationPathRepository
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.result.GetGuideResult
import javax.inject.Inject

class GetObtenerDatosXMLUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val navigationPathRepository: NavigationPathRepository,
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke(guideContext: GuideContext?): GetGuideResult {
        if (guideContext == null){
            return GetGuideResult.Error
        }

        return when(guideContext){
            is GuideContext.Actual -> {
                val currentPath = getCurrentPath(guideContext.guide)
                guiaRepository.getXMLGuide(
                    GuideContext.Actual(
                        guide = guideContext.guide, currentGuidePath = GuidePath(currentPath)
                    )
                )
            }
            is GuideContext.Moving -> {
                guiaRepository.getXMLGuide(
                    GuideContext.Actual(
                        guide = guideContext.guide, currentGuidePath = guideContext.oldGuidePath
                    )
                )

            }
            else -> GetGuideResult.Error
        }
    }

    private fun getCurrentPath(guideDomainModel: GuideDomainModel) = if (guideDomainModel.version == GuideVersion.V1) {
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
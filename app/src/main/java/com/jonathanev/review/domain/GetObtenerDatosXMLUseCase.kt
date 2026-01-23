package com.jonathanev.review.domain

import com.jonathanev.review.data.filesystem.FilePathsProvider
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.service.FileNamingRules
import javax.inject.Inject

class GetObtenerDatosXMLUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(guideContext: GuideContext?): GetGuideResult {
        if (guideContext == null){
            return GetGuideResult.Error
        }

        return when(guideContext){
            is GuideContext.Actual -> {
                /*val currentPath = getCurrentPath(guideContext.guide)
                val file = FileNamingRules.buildXmlFileName(guideContext.guide.nameGuide)*/
                guiaRepository.getXMLGuide(
                    GuideContext.Actual(
                        guide = guideContext.guide
                    )
                )
            }
            is GuideContext.Moving -> {
                val currentPath = getCurrentPath(guideContext.guide, guideContext.oldGuidePath.value)
                val file = FileNamingRules.buildXmlFileName(guideContext.guide.nameGuide)

                guiaRepository.getXMLGuide(
                    file,
                    GuideContext.Actual(
                        guide = guideContext.guide, currentGuidePath = GuidePath(currentPath)
                    )
                )
            }
            // Verificar que el renombrar se haga correctamente
            is GuideContext.Rename -> {
                val file = FileNamingRules.buildXmlFileName(guideContext.guide.nameGuide)

                guiaRepository.getXMLGuide(
                    file,
                    GuideContext.Actual(
                        guide = guideContext.guide, currentGuidePath = GuidePath(guideContext.currentGuidePath.value)
                    )
                )

            }

            is GuideContext.DeleteGuide -> GetGuideResult.Error
        }
    }
}
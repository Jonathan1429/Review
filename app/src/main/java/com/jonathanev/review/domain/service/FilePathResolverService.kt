package com.jonathanev.review.domain.service

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class FilePathResolverService @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository,
    private val filePathsProvider: FilePathsProvider
) {
    fun mapToFilePathActual(guideContext: GuideContext.Actual) = getCurrentPath(guideContext.guide, navigationPathRepository.getPathGuides().value)

    fun mapToImagePathActual(guideContext: GuideContext.Actual) = getImagePath(guideContext.guide, navigationPathRepository.getPathImages().value)
    fun mapToFilePathDelete(guideContext: GuideContext.DeleteGuide) = getCurrentPath(guideContext.guide, navigationPathRepository.getPathGuides().value)
    fun mapToFilePathRename(guideContext: GuideContext.Rename) = getCurrentPath(guideContext.guide, navigationPathRepository.getPathGuides().value)

    // Movimiento - ORIGEN
    fun mapToSourceGuidePath(guideContext: GuideContext.Moving) = getCurrentPath(guideContext.guide, guideContext.oldGuidePath.value)
    fun mapToSourceImagePath(guideContext: GuideContext.Moving) = getImagePath(guideContext.guide, guideContext.oldImagePath.value)

    // Movimiento - DESTINO
    fun mapToTargetGuidePath(guideContext: GuideContext.Moving) = getCurrentPath(guideContext.guide, navigationPathRepository.getPathGuides().value)

    private fun getCurrentPath(
        guideDomainModel: GuideDomainModel,
        basePath: String
    ) = if (guideDomainModel.version == GuideVersion.V1) {
        val file = FileNamingRules.buildXmlFileName(guideDomainModel.nameGuide)
        filePathsProvider.buildGuide(
            basePath,
            file
        )
    } else {
        val file = FileNamingRules.buildXmlFileName(guideDomainModel.nameGuide)
        filePathsProvider.buildFolderGuide(
            basePath,
            guideDomainModel.nameGuide,
            file
        )
    }

    private fun getImagePath(
        guideDomainModel: GuideDomainModel,
        basePath: String
    ) = if (guideDomainModel.version == GuideVersion.V1) {
        basePath
    } else {
        filePathsProvider.buildImage(
            basePath,
            guideDomainModel.nameGuide
        )
    }

    fun getPathGuidesV2(guideDomainModel: GuideDomainModel): String {
        val file = FileNamingRules.buildXmlFileName(guideDomainModel.nameGuide)
        return filePathsProvider.buildFolderGuide(
            navigationPathRepository.getPathGuides().value,
            guideDomainModel.nameGuide,
            file
        )
    }
    fun renamePathGuidesV2(guideContext: GuideContext.Rename): String {
        val file = FileNamingRules.buildXmlFileName(guideContext.name.value)
        return filePathsProvider.buildFolderGuide(
            navigationPathRepository.getPathGuides().value,
            guideContext.name.value,
            file
        )
    }
}
package com.jonathanev.review.domain.service

import com.jonathanev.review.data.filesystem.FilePathsProvider
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class FilePathResolverService @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository,
    private val filePathsProvider: FilePathsProvider
) {
    fun mapToFilePath(guideContext: GuideContext.Actual) = getCurrentPath(guideContext.guide)

    private fun getCurrentPath(guideDomainModel: GuideDomainModel, basePath: String = navigationPathRepository.currentPathGuides.value) = if (guideDomainModel.version == GuideVersion.V1) {
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
}
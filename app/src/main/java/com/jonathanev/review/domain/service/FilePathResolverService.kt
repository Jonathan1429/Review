package com.jonathanev.review.domain.service

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.FilePathResolver
import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class FilePathResolverService @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository,
    private val filePathsProvider: FilePathsProvider
): FilePathResolver {
    override fun mapToFilePathSpecificGuide(
        guideDomainModel: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath,
        kind: PathKind
    ) = getFilePathSpecificGuide(guideDomainModel, relativeGuidePath, kind)

    fun mapToFolderPathSpecificGuide(
        guideDomainModel: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath,
        kind: PathKind
    ) = getFolderPathSpecificGuide(guideDomainModel,relativeGuidePath, kind)

    fun mapToJoinRelativePath(
        relativeGuidePath: RelativeGuidePath,
        nameFolder: String
    ) = getRelativePath(relativeGuidePath, nameFolder)

    private fun getRelativePath(
        relativeGuidePath: RelativeGuidePath,
        nameFolder: String
    ) = RelativeGuidePath("${relativeGuidePath.value}/$nameFolder")

    override fun mapToFolderPath(
        relativeGuidePath: RelativeGuidePath,
        kind: PathKind
    ) = getFolderPath(relativeGuidePath, kind)

    private fun getFolderPath(
        relativeGuidePath: RelativeGuidePath,
        kind: PathKind
    ): GuidePath {
        val root = when (kind) {
            PathKind.GUIAS -> navigationPathRepository.getRootGuides()
            PathKind.IMAGENES -> navigationPathRepository.getRootImages()
        }

        val path = filePathsProvider.buildFolder(
            base = root.value,
            folder = relativeGuidePath.value
        )

        return GuidePath(path)
    }

    private fun getFilePathSpecificGuide(
        guideDomainModel: GuideDomainModel,
        relativePath: RelativeGuidePath,
        kind: PathKind
    ): GuidePath {
        val root = when (kind) {
            PathKind.GUIAS -> navigationPathRepository.getRootGuides()
            PathKind.IMAGENES -> navigationPathRepository.getRootImages()
        }

        val pathRelative = if (relativePath.value.isBlank()) root.value else "${root.value}/${relativePath.value}"
        val path = if (guideDomainModel.version == GuideVersion.V1) {
            val file = FileNamingRules.buildXmlFileName(guideDomainModel.nameGuide)
            filePathsProvider.buildGuide(
                base = pathRelative,
                file = file
            )
        } else {
            val file = FileNamingRules.buildXmlFileName(guideDomainModel.nameGuide)
            filePathsProvider.buildFolderGuide(
                base = pathRelative,
                folder = guideDomainModel.nameGuide,
                file = file
            )
        }

        return GuidePath(path)
    }

    private fun getFolderPathSpecificGuide(
        guideDomainModel: GuideDomainModel,
        relativePath: RelativeGuidePath,
        kind: PathKind
    ): GuidePath {
        val root = when (kind) {
            PathKind.GUIAS -> navigationPathRepository.getRootGuides()
            PathKind.IMAGENES -> navigationPathRepository.getRootImages()
        }

        val pathRelative = if (guideDomainModel.version == GuideVersion.V2)
            "$relativePath/${guideDomainModel.nameGuide}" else relativePath.value
        val path = filePathsProvider.buildFolder(
                base = root.value,
                folder = pathRelative
            )

        return GuidePath(path)
    }

    override fun getPathGuidesV2(guideDomainModel: GuideDomainModel): String {
        val file = FileNamingRules.buildXmlFileName(guideDomainModel.nameGuide)
        return filePathsProvider.buildFolderGuide(
            navigationPathRepository.getRootGuides().value,
            guideDomainModel.nameGuide,
            file
        )
    }

    override fun renamePathGuidesV2(guideContext: GuideContext.Rename): String {
        val file = FileNamingRules.buildXmlFileName(guideContext.name.value)
        return filePathsProvider.buildFolderGuide(
            base = navigationPathRepository.getRootGuides().value,
            folder = guideContext.name.value,
            file = file
        )
    }
}
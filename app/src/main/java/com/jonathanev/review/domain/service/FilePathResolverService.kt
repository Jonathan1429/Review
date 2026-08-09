package com.jonathanev.review.domain.service

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.HasOriginPath
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.FilePathResolver
import com.jonathanev.review.domain.repository.NavigationPathRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class FilePathResolverService @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository,
    private val filePathsProvider: FilePathsProvider
) : FilePathResolver {
    override suspend fun mapToFilePathSpecificGuide(
        guideDomainModel: GuideDomainModel,
        kind: PathKind
    ) = getFilePathSpecificGuide(guideDomainModel, kind)

    override fun mapToOldFolderPathSpecificGuide(
        guideDomainModel: GuideDomainModel,
        originContext: HasOriginPath,
        kind: PathKind
    ): GuidePath {
        return getOldFolderPathSpecificGuide(
            guideDomainModel,
            originContext.oldRelativeGuidePath,
            kind
        )
    }

    override suspend fun mapToFolderPathSpecificGuide(
        guideDomainModel: GuideDomainModel,
        kind: PathKind
    ) = getFolderPathSpecificGuide(guideDomainModel, kind)

    override suspend fun mapToJoinRelativePath(
        nameFolder: String
    ) = getRelativePath(nameFolder)

    private suspend fun getRelativePath(
        nameFolder: String
    ): RelativeGuidePath {
        val relativeGuidePath =
            navigationPathRepository.getRelativePath() // ESTE NO SE DEBERÍA USAR

        return RelativeGuidePath("${relativeGuidePath.value}/$nameFolder")
    }

    override suspend fun mapToFolderPath(
        kind: PathKind
    ) = getFolderPath(kind)

    private suspend fun getFolderPath(
        kind: PathKind
    ): GuidePath {
        val relativePath = navigationPathRepository.getRelativePath()

        val root = when (kind) {
            PathKind.GUIAS -> navigationPathRepository.getRootGuides()
            PathKind.IMAGENES -> navigationPathRepository.getRootImages()
        }

        val path = filePathsProvider.buildFolder(
            base = root.value,
            folder = relativePath.value
        )

        return GuidePath(path)
    }

    private suspend fun getFilePathSpecificGuide(
        guideDomainModel: GuideDomainModel,
        kind: PathKind
    ): GuidePath = withContext(Dispatchers.IO) {
        val relativePath = navigationPathRepository.getRelativePath()

        val root = when (kind) {
            PathKind.GUIAS -> navigationPathRepository.getRootGuides()
            PathKind.IMAGENES -> navigationPathRepository.getRootImages()
        }

        val pathRelative = if (relativePath.value.isBlank()) {
            root.value
        } else {
            File(root.value, relativePath.value).path
        }

        val file = FileNamingRules.buildXmlFileName(guideDomainModel.nameGuide)
        val path = if (guideDomainModel.version == GuideVersion.V1) {
            filePathsProvider.buildGuide(
                base = pathRelative,
                file = file
            )
        } else {
            filePathsProvider.buildFolderGuide(
                base = pathRelative,
                folder = guideDomainModel.nameGuide,
                file = file
            )
        }

        GuidePath(path)
    }

    private suspend fun getFolderPathSpecificGuide(
        guideDomainModel: GuideDomainModel,
        kind: PathKind
    ): GuidePath {
        val relativePath = navigationPathRepository.getRelativePath()

        val root = when (kind) {
            PathKind.GUIAS -> navigationPathRepository.getRootGuides()
            PathKind.IMAGENES -> navigationPathRepository.getRootImages()
        }

        val pathRelative = if (guideDomainModel.version == GuideVersion.V2)
            "${relativePath.value}/${guideDomainModel.nameGuide}" else relativePath.value
        val path = filePathsProvider.buildFolder(
            base = root.value,
            folder = pathRelative
        )

        return GuidePath(path)
    }

    private fun getOldFolderPathSpecificGuide(
        guideDomainModel: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath,
        kind: PathKind
    ): GuidePath {
        val root = when (kind) {
            PathKind.GUIAS -> navigationPathRepository.getRootGuides()
            PathKind.IMAGENES -> navigationPathRepository.getRootImages()
        }

        // Folder donde se guarda la guia
        var path = if (relativeGuidePath.value.isBlank()) {
            root.value
        } else {
            filePathsProvider.buildFolder(root.value, relativeGuidePath.value)
        }

        // Las guias V2 se guardan en su propio folder
        path = if (guideDomainModel.version == GuideVersion.V2) {
            filePathsProvider.buildFolder(path, guideDomainModel.nameGuide)
        } else {
            path
        }

        return GuidePath(path)
    }

    override fun getPathGuidesV1(
        guideDomainModel: GuideDomainModel,
        kind: PathKind,
        relativeGuidePath: RelativeGuidePath
    ): String {
        val root = when (kind) {
            PathKind.GUIAS -> navigationPathRepository.getRootGuides()
            PathKind.IMAGENES -> navigationPathRepository.getRootImages()
        }

        val relativeGuidePath = "${root.value}/${relativeGuidePath.value}"
        val file = FileNamingRules.buildXmlFileName(guideDomainModel.nameGuide)
        return filePathsProvider.buildGuide(relativeGuidePath, file)
    }

    override fun getPathGuidesV2(
        guideDomainModel: GuideDomainModel,
        kind: PathKind,
        relativeGuidePath: RelativeGuidePath
    ): String {
        val root = when (kind) {
            PathKind.GUIAS -> navigationPathRepository.getRootGuides()
            PathKind.IMAGENES -> navigationPathRepository.getRootImages()
        }

        val relativeGuidePath = "${relativeGuidePath.value}/${guideDomainModel.nameGuide}"
        val file = FileNamingRules.buildXmlFileName(guideDomainModel.nameGuide)
        return filePathsProvider.buildFolderGuide(
            root.value,
            relativeGuidePath,
            file
        )
    }

    /*override fun renamePathGuidesV2(guideContext: GuideContext.Rename): String {
        val file = FileNamingRules.buildXmlFileName(guideContext.name.value)
        return filePathsProvider.buildFolderGuide(
            base = navigationPathRepository.getRootGuides().value,
            folder = guideContext.name.value,
            file = file
        )
    }*/
}
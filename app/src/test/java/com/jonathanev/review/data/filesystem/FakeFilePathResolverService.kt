package com.jonathanev.review.data.filesystem

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.FilePathResolver
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.service.FileNamingRules
import java.io.File
import javax.inject.Inject

class FakeFilePathResolverService @Inject constructor(
    private val baseDir: File,
    private val navigationPathRepository: NavigationPathRepository,
    private val filePathsProvider: FilePathsProvider
) : FilePathResolver {
    override fun mapToFilePathSpecificGuide(
        guideDomainModel: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath,
        kind: PathKind
    ): GuidePath {
        val guideDir = File(baseDir, guideDomainModel.nameGuide)
        guideDir.mkdirs()

        return GuidePath(
            File(guideDir, "${guideDomainModel.nameGuide}.xml").absolutePath
        )
    }

    override fun mapToJoinRelativePath(
        relativeGuidePath: RelativeGuidePath,
        nameFolder: String
    ) = getRelativePath(relativeGuidePath, nameFolder)

    override fun mapToFolderPath(
        relativeGuidePath: RelativeGuidePath,
        kind: PathKind
    ): GuidePath {
        return GuidePath("")
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

    private fun getRelativePath(
        relativeGuidePath: RelativeGuidePath,
        nameFolder: String
    ) = RelativeGuidePath("${relativeGuidePath.value}/$nameFolder")
}

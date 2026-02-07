package com.jonathanev.review.data.filesystem

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.FilePathResolver
import java.io.File

class FakeFilePathResolverService(
    private val baseDir: File
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

    override fun mapToFolderPath(
        relativeGuidePath: RelativeGuidePath,
        kind: PathKind
    ): GuidePath {
        return GuidePath("")
    }

    override fun renamePathGuidesV2(guideContext: GuideContext.Rename): String {
        return ""
    }

    override fun getPathGuidesV2(
        guideDomainModel: GuideDomainModel
    ): String {
        val guideDir = File(baseDir, guideDomainModel.nameGuide)
        guideDir.mkdirs()

        return File(
            guideDir,
            "${guideDomainModel.nameGuide}.xml"
        ).absolutePath
    }
}

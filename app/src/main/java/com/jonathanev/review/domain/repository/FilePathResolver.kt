package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.model.RelativeGuidePath

interface FilePathResolver {
    fun mapToFilePathSpecificGuide(
        guideDomainModel: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath,
        kind: PathKind
    ): GuidePath

    fun mapToFolderPath(
        relativeGuidePath: RelativeGuidePath,
        kind: PathKind
    ): GuidePath

    fun renamePathGuidesV2(guideContext: GuideContext.Rename): String

    fun getPathGuidesV2(guideDomainModel: GuideDomainModel): String
}
package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.model.RelativeGuidePath

interface FilePathResolver {
    suspend fun mapToFilePathSpecificGuide(
        guideDomainModel: GuideDomainModel,
        kind: PathKind
    ): GuidePath

    fun mapToJoinRelativePath(
        relativeGuidePath: RelativeGuidePath,
        nameFolder: String
    ): RelativeGuidePath

    fun mapToFolderPath(
        relativeGuidePath: RelativeGuidePath,
        kind: PathKind
    ): GuidePath

    //fun renamePathGuidesV2(guideContext: GuideContext.Rename): String

    fun getPathGuidesV2(
        guideDomainModel: GuideDomainModel,
        kind: PathKind,
        relativeGuidePath: RelativeGuidePath
    ): String

    fun getPathGuidesV1(
        guideDomainModel: GuideDomainModel,
        kind: PathKind,
        relativeGuidePath: RelativeGuidePath
    ): String
}
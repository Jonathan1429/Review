package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideRenameContext
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.RelativeGuidePath

interface ImagesRepository {
    fun save(
        image: QuestionContentDomain.Image,
        guide: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath
    )

    fun moveImages(
        images: List<QuestionContentDomain.Image>,
        guideRenameContext: GuideRenameContext,
        relativeGuidePath: RelativeGuidePath
    ): Boolean

    fun deleteImages(
        guide: GuideDomainModel,
        images: List<QuestionContentDomain.Image>,
        relativeGuidePath: RelativeGuidePath
    ): Boolean

    fun moveUnassignedImages(movedFiles: List<String>)
}
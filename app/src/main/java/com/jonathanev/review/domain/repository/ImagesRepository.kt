package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideRenameContext
import com.jonathanev.review.domain.model.QuestionContentDomain

interface ImagesRepository {
    suspend fun save(
        image: QuestionContentDomain.Image,
        guide: GuideDomainModel
    )

    fun moveImages(
        images: List<QuestionContentDomain.Image>,
        guideRenameContext: GuideRenameContext
    ): Boolean

    fun deleteImages(
        guide: GuideDomainModel,
        images: List<QuestionContentDomain.Image>
    ): Boolean

    fun moveUnassignedImages()
}
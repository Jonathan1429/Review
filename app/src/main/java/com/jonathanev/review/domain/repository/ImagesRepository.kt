package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideRenameContext
import com.jonathanev.review.domain.model.QuestionContentDomain

interface ImagesRepository {
    suspend fun save(
        image: QuestionContentDomain.Image,
        guide: GuideDomainModel
    )

    suspend fun saveTempImage(uriString: String): String
    suspend fun clearTempImages()
    suspend fun moveImages(
        images: List<QuestionContentDomain.Image>,
        guideRenameContext: GuideRenameContext
    ): Boolean

    suspend fun deleteImages(
        guide: GuideDomainModel,
        images: List<QuestionContentDomain.Image>,
    ): Boolean

    suspend fun moveUnassignedImages(movedFiles: List<String>)
}
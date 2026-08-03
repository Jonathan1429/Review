package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.ImageContext
import com.jonathanev.review.domain.model.QuestionContentDomain

interface DirectoryManager {
    suspend fun createPathImages(
        guideDomainModel: GuideDomainModel,
        isNewFile: Boolean
    ): Boolean

    suspend fun moveImages(
        guideDomainModel: GuideDomainModel,
        imageContext: ImageContext,
        images: List<QuestionContentDomain.Image>
    ): Boolean

    suspend fun getImagesInDevice(guideDomain: GuideDomainModel): Set<String>
    suspend fun deleteLeftoverImagesInDevice(
        guideDomainModel: GuideDomainModel,
        listImages: List<QuestionContentDomain.Image>
    )

    fun existPath(path: String): Boolean
    suspend fun createPathGuide(guideDomainModel: GuideDomainModel): Boolean
    fun deleteFolderEmpty(context: GuideContext.Moving)
    fun createFoldersMain(): Boolean
}
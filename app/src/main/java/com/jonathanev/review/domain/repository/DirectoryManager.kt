package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.ImageSource
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain

interface DirectoryManager {
    fun createPathImages(guideDomainModel: GuideDomainModel, isNewFile: Boolean): Boolean
    fun moveImages(
        guideDomain: GuideDomainModel,
        imageSource: ImageSource,
        images: List<QuestionContentDomain.Image>
    ): Boolean

    fun getImagesInDevice(guideDomain: GuideDomainModel): Set<String>
    fun deleteLeftoverImagesInDevice(
        nameGuide: String,
        listImages: List<QuestionContentDomain.Image>
    )

    fun existPath(path: String): Boolean
    fun createPathGuide(nameGuide: String): Boolean
    fun prepareGuidePath(newName: String): Boolean
    fun deleteFolderEmpty(guideContext: GuideContext.Actual): Boolean
    fun createFoldersMain(): Boolean
}
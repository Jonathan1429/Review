package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.ImageSource
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.RelativeGuidePath

interface DirectoryManager {
    fun createPathImages(guideDomainModel: GuideDomainModel, isNewFile: Boolean): Boolean
    fun moveImages(
        guideDomainModel: GuideDomainModel,
        imageSource: ImageSource,
        images: List<QuestionContentDomain.Image>
    ): Boolean

    fun getImagesInDevice(guideDomain: GuideDomainModel, relativeGuidePath: RelativeGuidePath): Set<String>
    fun deleteLeftoverImagesInDevice(
        nameGuide: String,
        listImages: List<QuestionContentDomain.Image>
    )

    fun existPath(path: String): Boolean
    fun createPathGuide(nameGuide: String): Boolean
    fun prepareGuidePath(newName: String): Boolean
    fun deleteFolderEmpty(context: GuideContext.Moving): Boolean
    fun createFoldersMain(): Boolean
}
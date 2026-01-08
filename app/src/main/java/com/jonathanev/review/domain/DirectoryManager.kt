package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain

interface DirectoryManager {
    fun prepareCleanDirectory(guideDomainModel: GuideDomainModel, isNewFile: Boolean)
    fun moveImagesV1(
        listImages: List<QuestionContentDomain.Image>,
        nameGuide: String
    )
    fun deleteLeftoverImagesInDevice(nameGuide: String, listImages: List<QuestionContentDomain.Image>)
    fun existPath(path: String): Boolean
    fun createPathGuide(nameGuide: String)
}
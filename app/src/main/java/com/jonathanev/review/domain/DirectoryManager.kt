package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QuestionContentDomain

interface DirectoryManager {
    fun prepareCleanDirectory(path: String, isNewFile: Boolean)
    fun moveImagesV1(
        listImages: List<QuestionContentDomain.Image>,
        nameGuide: String
    )
    fun deleteLeftoverImagesInDevice(nameGuide: String, listImages: List<QuestionContentDomain.Image>)
    fun existPath(path: String): Boolean
}
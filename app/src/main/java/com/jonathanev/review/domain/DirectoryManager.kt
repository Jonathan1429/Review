package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import java.io.File

interface DirectoryManager {
    fun createPathImages(guideDomainModel: GuideDomainModel, isNewFile: Boolean)
    fun moveImages(
        listImages: List<QuestionContentDomain.Image>,
        nameGuide: String,
        version: String,
        oldPath: File? = null
    )
    fun deleteLeftoverImagesInDevice(nameGuide: String, listImages: List<QuestionContentDomain.Image>)
    fun existPath(path: String): Boolean
    fun createPathGuide(nameGuide: String)
    fun deleteFolderEmpty(oldPathGuide: String)
}
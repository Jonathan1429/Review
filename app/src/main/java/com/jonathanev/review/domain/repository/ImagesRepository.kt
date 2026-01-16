package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain

interface ImagesRepository {
    suspend fun save(image: QuestionContentDomain.Image, nameFolder: String)
    fun reubicarImagenes(
        fileName: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        attributesGuide: GuideDomainModel
    ): Boolean
    fun delete(
        guideDomainModel: GuideDomainModel,
        listImages: List<QuestionContentDomain.Image>
    ): Boolean
    fun movingImagesToOtros()
}
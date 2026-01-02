package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import java.io.File

interface ImagesRepository {
    suspend fun saveImage(image: QuestionContentDomain.Image, imagesPath: File)
    fun reubicarImagenes(
        version: String,
        fileName: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>
    )
}
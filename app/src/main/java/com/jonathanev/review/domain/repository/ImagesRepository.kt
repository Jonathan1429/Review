package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.QuestionContentDomain
import java.io.File

interface ImagesRepository {
    suspend fun saveImage(image: QuestionContentDomain.Image, imagesPath: File)
}
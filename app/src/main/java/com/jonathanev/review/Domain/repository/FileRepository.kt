package com.jonathanev.review.Domain.repository

import com.jonathanev.review.presentation.model.QuestionContentDomain
import java.io.File

interface FileRepository {
    /*fun setFilesInCurrentPath()
    fun getFilesInCurrentPath(): List<GuiaModel>*/
    fun setCurrentPath(path: String)
    fun getCurrentPath(): String
    suspend fun saveImage(image: QuestionContentDomain.Image, imagesPath: File)
}
package com.jonathanev.review.Domain.repository

import com.jonathanev.review.Data.Model.GuiaModel
import java.io.File

interface FileRepository {
    /*fun setFilesInCurrentPath()
    fun getFilesInCurrentPath(): List<GuiaModel>*/
    fun setCurrentPath(path: String)
    fun getCurrentPath(): String
}
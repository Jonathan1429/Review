package com.jonathanev.review.Domain.repository

interface FileRepository {
    /*fun setFilesInCurrentPath()
    fun getFilesInCurrentPath(): List<GuiaModel>*/
    fun setCurrentPath(path: String)
    fun getCurrentPath(): String
}
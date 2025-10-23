package com.jonathanev.review.Domain.repository

import java.io.File

interface FileRepository {
    fun getFilesInCurrentPath(): List<File>
    fun setCurrentPath(path: String)
    fun getCurrentPath(): String
}
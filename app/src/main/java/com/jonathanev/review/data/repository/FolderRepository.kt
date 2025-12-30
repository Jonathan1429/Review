package com.jonathanev.review.data.repository

import com.jonathanev.review.data.Model.prueba.FolderModel
import java.io.File

interface FolderRepository {
    fun getAttributesFolder(folderPath: File): FolderModel
}
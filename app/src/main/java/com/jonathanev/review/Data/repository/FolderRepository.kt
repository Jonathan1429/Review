package com.jonathanev.review.Data.repository

import com.jonathanev.review.Data.Model.prueba.FolderModel
import java.io.File

interface FolderRepository {
    fun getAttributesFolder(folderPath: File): FolderModel
}
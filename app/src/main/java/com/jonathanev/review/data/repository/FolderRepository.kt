package com.jonathanev.review.data.repository

import com.jonathanev.review.presentation.model.FolderUiModel
import java.io.File

interface FolderRepository {
    fun getAttributesFolder(folderPath: File): FolderUiModel
}
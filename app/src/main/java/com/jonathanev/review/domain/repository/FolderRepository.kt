package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.FolderDomainModel
import java.io.File

interface FolderRepository {
    fun getAttributesFolder(folderPath: File): FolderDomainModel
    fun getFolders(): List<FolderDomainModel>
}
package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.model.RelativeGuidePath


interface FolderRepository {
    fun getFolders(): List<FolderDomainModel>
    fun deleteFolder(nameFolder: String): Boolean
}
package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.presentation.event.UIStopEvent


interface FolderRepository {
    //fun getAttributesFolder(folderPath: File): FolderDomainModel
    fun getFolders(): List<FolderDomainModel>
    fun deleteFolder(nameFolder: String): UIStopEvent
}
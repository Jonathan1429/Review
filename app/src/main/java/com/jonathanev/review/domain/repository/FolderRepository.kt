package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.FolderWithNumGuidesDomainModel

interface FolderRepository {
    //fun getAttributesFolder(folderPath: File): FolderDomainModel
    fun getFolders(): List<FolderWithNumGuidesDomainModel>
}
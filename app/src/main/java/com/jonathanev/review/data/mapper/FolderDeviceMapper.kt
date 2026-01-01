package com.jonathanev.review.data.mapper

import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.data.FolderFileModel

fun FolderFileModel.toDomain(): FolderDomainModel{
    return FolderDomainModel(name = this.name)
}
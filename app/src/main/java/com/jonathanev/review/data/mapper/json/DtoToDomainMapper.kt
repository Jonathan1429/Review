package com.jonathanev.review.data.mapper.json

import com.jonathanev.review.data.model.AttributesFolderDto
import com.jonathanev.review.domain.model.FolderAttributesDomain

fun AttributesFolderDto.toDomain(): FolderAttributesDomain {
    return FolderAttributesDomain(
        name = this.name,
        imgFolder = this.imgFolder,
        color = this.color
    )
}
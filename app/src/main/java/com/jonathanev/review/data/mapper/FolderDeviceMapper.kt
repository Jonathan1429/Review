package com.jonathanev.review.data.mapper

import com.jonathanev.review.domain.model.FolderAttributesDomain
import com.jonathanev.review.data.AttributesFolderModel

fun AttributesFolderModel.toIconType(): FolderAttributesDomain{
    return FolderAttributesDomain(
        name = this.name,
        imgFolder = this.imgFolder,
        color = this.color
    )
}
package com.jonathanev.review.presentation.mapper

import com.jonathanev.review.data.mapper.toIconKeys
import com.jonathanev.review.domain.model.FolderScreenInfoDomain
import com.jonathanev.review.presentation.model.ScreenDataUi
import com.jonathanev.review.ui.mapper.toInt

fun ScreenDataUi.toDomain(): FolderScreenInfoDomain {
    return FolderScreenInfoDomain(
        name = this.name,
        description = this.description,
        imgFolder = this.imgFolder.toIconKeys(),
        color = this.color.toInt(),
        version = this.version
    )
}
package com.jonathanev.review.data.mapper.json

import com.jonathanev.review.data.model.json.ScreenDataDto
import com.jonathanev.review.domain.model.ScreenDataDomain

fun ScreenDataDomain.toDto(): ScreenDataDto {
    return ScreenDataDto(
        name = this.name,
        description = this.description,
        imgFolder = this.imgFolder,
        color = this.color,
        version =this.version
    )
}
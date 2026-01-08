package com.jonathanev.review.data.provider

import com.jonathanev.review.data.model.AttributesFolderJson
import com.jonathanev.review.domain.model.FolderAttributesDomain
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.IconType
import javax.inject.Inject

class DefaultFolderAttributesProvider @Inject constructor() {
    fun default(name: String): AttributesFolderJson {
        return AttributesFolderJson(
            name = name,
            imgFolder = "",
            color = 0
        )
    }
}

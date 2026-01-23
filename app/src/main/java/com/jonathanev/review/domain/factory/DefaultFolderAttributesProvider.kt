package com.jonathanev.review.domain.factory

import com.jonathanev.review.domain.model.FolderAttributesDomain
import javax.inject.Inject

object DefaultFolderAttributesProvider {
    fun default(name: String): FolderAttributesDomain {
        return FolderAttributesDomain(
            name = name,
            imgFolder = "",
            color = 0
        )
    }
}
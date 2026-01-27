package com.jonathanev.review.presentation.mapper

import com.jonathanev.review.data.mapper.toColorType
import com.jonathanev.review.data.mapper.toIconType
import com.jonathanev.review.domain.model.FolderAttributesDomain
import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.result.FolderResultDomain
import com.jonathanev.review.presentation.folders.model.FolderUiModel
import com.jonathanev.review.presentation.model.FolderAttributesUi
import com.jonathanev.review.presentation.model.FolderResultUi

fun FolderDomainModel.toUi(): FolderUiModel{
    return FolderUiModel(
        folder = this.folder.toUi(),
        numGuides = this.numGuides
    )
}

fun FolderAttributesDomain.toUi(): FolderAttributesUi {
    return FolderAttributesUi(
        name = name,
        imgFolder = imgFolder.toIconType(), // IconType
        color = color.toColorType()         // ColorType
    )
}

fun FolderResultDomain.toUi(): FolderResultUi {
    return when(this){
        is FolderResultDomain.Error -> FolderResultUi.Error(this.message)
        is FolderResultDomain.Success -> FolderResultUi.Success(this.folderDomain.toUi())
    }
}
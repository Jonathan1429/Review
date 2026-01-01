package com.jonathanev.review.presentation.mapper

import android.graphics.Color
import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.model.FolderWithNumGuidesDomainModel
import com.jonathanev.review.R
import com.jonathanev.review.presentation.folders.model.FolderUiModel

fun FolderWithNumGuidesDomainModel.toUi(): FolderUiModel {
    return FolderUiModel(
        name = this.folder.name,
        imgFolder = R.drawable.ic_anchor_solid_full,
        color = Color.BLACK,
        numGuides = this.numGuides
    )
}

fun FolderUiModel.toDomain(): FolderDomainModel {
    return FolderDomainModel(
        name = this.name,
    )
}

fun FolderUiModel.toDomainWithFolders(): FolderWithNumGuidesDomainModel {
    return FolderWithNumGuidesDomainModel(FolderDomainModel(this.name), this.numGuides)
}
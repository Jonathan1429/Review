package com.jonathanev.review.presentation.mapper

import androidx.annotation.DrawableRes
import com.jonathanev.review.R
import com.jonathanev.review.domain.model.FolderAttributesDomain
import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.presentation.folders.model.FolderResultDomain
import com.jonathanev.review.presentation.folders.model.FolderUiModel
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.FolderAttributesUi
import com.jonathanev.review.presentation.model.FolderResultUi
import com.jonathanev.review.presentation.model.IconType

fun FolderDomainModel.toUi(): FolderUiModel{
    return FolderUiModel(
        folder = this.folder.toUi(),
        numGuides = this.numGuides
    )
}

fun FolderAttributesDomain.toUi(): FolderAttributesUi {
    return FolderAttributesUi(
        name = this.name,
        imgFolder = mapDrawable(this.imgFolder.toIconType()),
        color = mapColor(this.color.toColorType())
    )
}

private fun mapColor(colorType: ColorType): Int {
    return when(colorType){
        ColorType.Black -> R.color.black
        ColorType.Gray -> R.color.text_gray
        is ColorType.RandomColor -> colorType.color
        ColorType.White -> R.color.white
    }
}

@DrawableRes
private fun mapDrawable(iconType: IconType): Int {
    return when(iconType) {
        IconType.LIGHTBULB -> R.drawable.ic_lightbulb_solid_full
        IconType.ANCHOR_SOLID_FULL -> R.drawable.ic_anchor_solid_full
        IconType.ANGELLIST_BRANDS_SOLID_FULL -> R.drawable.ic_angellist_brands_solid_full
        IconType.BACTERIA_SOLID_FULL -> R.drawable.ic_bacteria_solid_full
    }
}

fun FolderResultDomain.toUi(): FolderResultUi {
    return when(this){
        is FolderResultDomain.Error -> FolderResultUi.Error(this.message)
        is FolderResultDomain.Success -> FolderResultUi.Success(this.folderDomain.toUi())
    }
}
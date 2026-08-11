package com.jonathanev.review.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FolderUiModel(
    val folder: FolderAttributesUi,
    val numGuides: Int
) : Parcelable
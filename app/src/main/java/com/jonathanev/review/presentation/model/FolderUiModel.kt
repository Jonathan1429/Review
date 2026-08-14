package com.jonathanev.review.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class FolderUiModel(
    val folder: FolderAttributesUi,
    val numGuides: Int
) : Parcelable
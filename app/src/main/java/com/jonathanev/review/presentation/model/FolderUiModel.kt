package com.jonathanev.review.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class FolderUiModel(
    val folder: FolderAttributesUi,
    val numGuides: Int
)
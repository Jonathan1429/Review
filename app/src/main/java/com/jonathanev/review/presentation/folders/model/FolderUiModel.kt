package com.jonathanev.review.presentation.folders.model

import com.jonathanev.review.presentation.model.FolderAttributesUi

data class FolderUiModel(
    val folder: FolderAttributesUi,
    val numGuides: Int
)
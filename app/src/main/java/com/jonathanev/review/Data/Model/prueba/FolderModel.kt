package com.jonathanev.review.Data.Model.prueba

import kotlinx.serialization.Serializable

@Serializable
data class FolderModel(
    val name: String,
    val description: String,
    val imgFolder: Int,
    val color: Int,
)
package com.jonathanev.review.Data.Model

data class ProcessResult(
    val type: TypeFile,
    val plainText: String,
    val colors: List<ColorPregModel>,
    val encryptedImagePath: String? = null
)

package com.jonathanev.review.data

import com.jonathanev.review.data.Model.prueba.FolderUI


sealed class FolderResult {
    data class Success(val folder: FolderUI) : FolderResult()
    data class Error(val message: String) : FolderResult()
}
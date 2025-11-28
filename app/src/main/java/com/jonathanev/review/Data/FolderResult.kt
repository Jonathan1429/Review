package com.jonathanev.review.Data

import com.jonathanev.review.Data.Model.prueba.FolderUI


sealed class FolderResult {
    data class Success(val folder: FolderUI) : FolderResult()
    data class Error(val message: String) : FolderResult()
}
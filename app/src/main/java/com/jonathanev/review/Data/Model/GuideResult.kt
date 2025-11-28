package com.jonathanev.review.Data.Model

import com.jonathanev.review.Data.FolderResult
import com.jonathanev.review.Data.Model.prueba.FolderUI

sealed class GuideResult {
    data class Success(val folder: GuideModel) : GuideResult()
    data class Error(val message: String) : GuideResult()
}
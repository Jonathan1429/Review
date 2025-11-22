package com.jonathanev.review.Data

import com.jonathanev.review.Data.Model.GuiaModel

sealed class GuiaResult {
    data class Success(val guia: GuiaModel) : GuiaResult()
    //data object Empty : GuiaResult()
    data class Error(val message: String) : GuiaResult()
}
package com.jonathanev.review.domain.result

sealed class ValidateCreateFileResult {
    data class Error(val message: String) : ValidateCreateFileResult()
    data class Success(val name: String, val description: String) : ValidateCreateFileResult()
}
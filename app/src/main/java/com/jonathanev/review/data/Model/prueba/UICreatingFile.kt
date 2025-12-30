package com.jonathanev.review.data.Model.prueba

sealed class UICreatingFile {
    data class Message(val message: String) : UICreatingFile()
    data class ContinuedProcess(val name: String, val description: String) : UICreatingFile()
    data class FileExisted(val name: String, val description: String): UICreatingFile()
}
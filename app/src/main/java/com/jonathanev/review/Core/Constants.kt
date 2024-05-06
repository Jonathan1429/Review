package com.jonathanev.review.Core

import android.annotation.SuppressLint
import java.io.File

@SuppressLint("SdCardPath")
object Constants {
    const val path = "/data/data/com.jonathanev.review/files"

    var file: File = File("/data/data/com.jonathanev.review/files/")

    fun changeFilePath(folderName: String) {
        file = File("/data/data/com.jonathanev.review/files/$folderName")
    }

    fun restoreMainFilePath() {
        // Restaurar la ruta principal
        file = File("/data/data/com.jonathanev.review/files/")
    }
}
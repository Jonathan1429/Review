package com.jonathanev.review.Core

import android.annotation.SuppressLint
import java.io.File

@SuppressLint("SdCardPath")
object Constants {
    const val path = "/data/data/com.jonathanev.review/files/guias"

    val rutaPrin = File("/data/data/com.jonathanev.review/files/")
    var file: File = File("/data/data/com.jonathanev.review/files/guias/")
    val fileImages = File("/data/data/com.jonathanev.review/files/imagenes/")
    val fileImagesPiv = File("/data/data/com.jonathanev.review/files/imagenesPivote/")

    const val PICK_IMAGE_REQUEST = 1
}
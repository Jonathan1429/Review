package com.jonathanev.review.Core

import android.annotation.SuppressLint
import java.io.File

@SuppressLint("SdCardPath")
object Constants {
    const val BASERUTA_IMG_CIFRADO = "frqwhqw://phgld/slfnhu/"
    const val BASERUTA_IMG = "content://media/picker/"
    const val PICK_IMAGE_REQUEST = 1

    // Guias y carpetas que no se pueden crear
    const val DATASTORE = "datastore"
    const val GUIAS = "guias"
    const val IMAGENES = "imagenes"
    const val IMAGENESPIVOTE = "imagenesPivote"
    const val PRINCIPAL = "Principal"
}
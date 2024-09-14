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

    const val baseRutaImagenCifrado = "frqwhqw://phgld/slfnhu/"
    const val baseRutaImagen = "content://media/picker/"
    const val PICK_IMAGE_REQUEST = 1

    // Guias y carpetas que no se pueden crear
    const val cons_dataStore = "datastore"
    const val cons_guias = "guias"
    const val cons_imagenes = "imagenes"
    const val cons_imagenesPiv = "imagenesPivote"
}
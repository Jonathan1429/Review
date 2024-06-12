package com.jonathanev.review.Domain

import java.io.File
import javax.inject.Inject

class getAllFolders @Inject constructor() {
    private var carpetasImagenes = mutableListOf<String>()

    operator fun invoke(file: File): List<String> {
        // Creo el array de tipo File con el contenido de la carpeta.
        // val files = arrayOf(file)
        val files = file.listFiles()

        // Hacemos un ciclo por cada fichero para extraer el nombre de cada uno.
        if (files!!.isNotEmpty()) {
            for (i in files.indices) {
                // Sacamos del array files el primer fichero.
                val archivo: File = files[i]
                var name = ""

                if (archivo.isDirectory) {                    // Folder (guias)
                    name = archivo.name
                    carpetasImagenes.add(name)
                }
            }

            val carpetasOrdenadas = carpetasImagenes.sortedBy { it }
            carpetasImagenes.clear()
            carpetasImagenes.addAll(carpetasOrdenadas)
            return carpetasImagenes
        } else {
            carpetasImagenes.clear()
            return carpetasImagenes
        }
    }
}
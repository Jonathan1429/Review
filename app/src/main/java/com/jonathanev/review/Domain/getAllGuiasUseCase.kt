package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.R
import java.io.File
import javax.inject.Inject

class getAllGuiasUseCase @Inject constructor(
    val getRandomGuiaImage: getRandomGuiaImage
){
    private var guias = mutableListOf<GuiaModel>()
    private var guiasPivote = mutableListOf<GuiaModel>()

    operator fun invoke(file: File): List<GuiaModel>? {
        // Creo el array de tipo File con el contenido de la carpeta.
        // val files = arrayOf(file)
        val files = file.listFiles()
        guias.clear()
        guiasPivote.clear()
        // Hacemos un ciclo por cada fichero para extraer el nombre de cada uno.
        if (files!!.isNotEmpty()) {
            var image = 0
            image = getRandomGuiaImage()

            for (i in files.indices) {
                // Sacamos del array files el primer fichero.
                val archivo: File = files[i]
                var name = ""

                if (archivo.name.contains(".xml")) {           // File (guiasPivote)
                    // Guardamos el nombre del fichero en la lista item.
                    name = archivo.name.replace(".xml".toRegex(), "")
                    guiasPivote.add(GuiaModel(name, image))
                } else if (archivo.isDirectory){                    // Folder (guias)
                    // image = R.drawable.img_carpeta
                    name = archivo.name
                    guias.add(GuiaModel(name, R.drawable.img_carpeta, true))
                }
            }

            val carpetasOrdenadas = guias.sortedBy { it.nombreGuia }
            guias.clear()
            val guiasOrdenadas = guiasPivote.sortedBy { it.nombreGuia }
            guias.addAll(carpetasOrdenadas)
            guias.addAll(guiasOrdenadas)
            return guias
        } else {
            guias.clear()
            return guias
        }
    }
}
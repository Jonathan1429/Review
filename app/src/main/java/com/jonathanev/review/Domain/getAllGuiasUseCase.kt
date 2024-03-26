package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants.file
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.R
import java.io.File
import javax.inject.Inject

class getAllGuiasUseCase @Inject constructor(
    val getRandomGuiaImage: getRandomGuiaImage
){
    private var guias = mutableListOf<GuiaModel>()

    operator fun invoke(): List<GuiaModel>? {
        // Creo el array de tipo File con el contenido de la carpeta.
        // val files = arrayOf(file)
        val files = file.listFiles()
        // Hacemos un ciclo por cada fichero para extraer el nombre de cada uno.
        if (files!!.isNotEmpty()) {

            for (i in files.indices) {
                // Sacamos del array files el primer fichero.
                val archivo: File = files[i]
                var name = ""
                var image = 0

                if (archivo.name.contains(".xml")) {
                    // Guardamos el nombre del fichero en la lista item.
                    name = archivo.name.replace(".xml".toRegex(), "")
                    image = getRandomGuiaImage()
                    guias.add(GuiaModel(name, image))
                } else if (archivo.isDirectory){
                    image = R.drawable.img_carpeta
                    name = archivo.name
                    guias.add(GuiaModel(name, image))
                }
            }
        }

        return guias.sortedBy { it.nombreGuia }
    }
}
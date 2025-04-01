package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants.file
import com.jonathanev.review.Data.Model.GuiaModel
import java.io.File
import javax.inject.Inject

class GetAllGuiasCarpetaSeleccionadaUseCase @Inject constructor(
    val getRandomGuiaImageUseCase: GetRandomGuiaImageUseCase
) {

    private var guias = mutableListOf<GuiaModel>()

    operator fun invoke(nombreCarpeta: String): List<GuiaModel>? {
        // Creo el array de tipo File con el contenido de la carpeta.
        // val files = arrayOf(file)
        val nuevaRuta = "$file/$nombreCarpeta/"
        var files = File(nuevaRuta)

        val directorios = files.listFiles()

        // Hacemos un ciclo por cada fichero para extraer el nombre de cada uno.
        if (directorios != null) {
            if (directorios.isNotEmpty()) {
                val image = getRandomGuiaImageUseCase()

                for (i in directorios.indices) {
                    // Sacamos del array files el primer fichero.
                    val archivo: File = directorios[i]

                    if (archivo.name.contains(".xml")) {
                        // Guardamos el nombre del fichero en la lista item.
                        val name = archivo.name.replace(".xml".toRegex(), "")

                        guias.add(GuiaModel(name, image))
                    }
                }
            }
        }

        val guiasOrdenadas = guias.sortedBy { it.nombreGuia }
        guias.clear()
        guias.addAll(guiasOrdenadas)
        return guias
    }
}
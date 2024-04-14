package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.GuiaProvider
import javax.inject.Inject

class getGuiaUseCase @Inject constructor(
    private val guiaProvider: GuiaProvider
){
    operator fun invoke(ruta: String): GuiaModel {
        val archivo = ruta.substringAfterLast("/")
        var guia: GuiaModel = GuiaModel("", 0)

        for ((posicion, valor) in guiaProvider.guias.withIndex()){
            val guiaXML = "${valor.nombreGuia}.xml"
            if (archivo == guiaXML){
                guia.nombreGuia = guiaProvider.guias[posicion].nombreGuia
                break
            }
        }

        return guia
    }
}
package com.jonathanev.review.Domain

import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.PreguntaRespuesta
import javax.inject.Inject

class getObtenerDatosXMLUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
){
    operator fun invoke(nombreArchivo: String, ruta: String): List<PreguntaRespuesta>{
        return guiaRepository.obtenerDatosXML(nombreArchivo, ruta)
    }
}
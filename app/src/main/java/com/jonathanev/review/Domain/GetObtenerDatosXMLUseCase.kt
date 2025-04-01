package com.jonathanev.review.Domain

import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.PreguntaRespuestaModel
import javax.inject.Inject

class GetObtenerDatosXMLUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
){
    operator fun invoke(nombreArchivo: String, ruta: String): List<PreguntaRespuestaModel>{
        return guiaRepository.obtenerDatosXML(nombreArchivo, ruta)
    }
}
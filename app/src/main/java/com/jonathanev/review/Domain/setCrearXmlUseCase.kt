package com.jonathanev.review.Domain

import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import javax.inject.Inject

class setCrearXmlUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(nombreArchivo: String, preguntas: ArrayList<String>, respuestas: ArrayList<String>): ValidacionesGuiaModel {
        return guiaRepository.saveFile(nombreArchivo, preguntas, respuestas)
    }
}
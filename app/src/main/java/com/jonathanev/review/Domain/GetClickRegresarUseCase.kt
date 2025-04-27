package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import javax.inject.Inject

class GetClickRegresarUseCase @Inject constructor(
    private var setPintarTextosUseCase: SetPintarTextosUseCase
) {
    operator fun invoke(
        contadorPregunta: Int,
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        ruta: String
    ): ValidacionesGuiaModel{
        return when {
            contadorPregunta == 0 -> {
                ValidacionesGuiaModel(
                    message = "Ya no tienes preguntas anteriores",
                )
            }
            else -> {
                val contador = contadorPregunta - 1

                val response =
                    setPintarTextosUseCase(true, preguntas, respuestas, contador, ruta)

                return response
            }
        }
    }
}
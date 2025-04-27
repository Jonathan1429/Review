package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import javax.inject.Inject

class GetClickSiguienteUseCase @Inject constructor(
    private var setPintarTextosUseCase: SetPintarTextosUseCase
) {
    operator fun invoke(
        contadorPregunta: Int,
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        ruta: String
    ): ValidacionesGuiaModel {
        val posPregFin = preguntas.size - 1
        val contador = contadorPregunta + 1

        // Si hay más preguntas pinta lo siguiente.
        return if (contador <= posPregFin) {
            // Pintamos texto o regresamos la pregunta
            setPintarTextosUseCase(
                isEtPregunta = true,
                preguntas,
                respuestas,
                contador,
                ruta
            )

        } else {
            // Si no hay más preguntas.
            ValidacionesGuiaModel(
                message = "Se acabaron las preguntas, ¿Quieres repetir la guia?",
            )
        }
    }
}
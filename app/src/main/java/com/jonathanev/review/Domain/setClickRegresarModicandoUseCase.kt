package com.jonathanev.review.Domain

import android.text.Editable
import com.jonathanev.review.Data.Model.EstadoPreguntasRespuestas
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import javax.inject.Inject

class setClickRegresarModicandoUseCase @Inject constructor(
    private val setSpanPalabraUseCase: setSpanPalabraUseCase,
    private val setColocarEtiquetasUseCase: setColocarEtiquetasUseCase,
    private val setPintarTextosUseCase: setPintarTextosUseCase
) {
    operator fun invoke(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        editable: Editable,
        isEtPregunta: Boolean
    ): ValidacionesGuiaModel {
        val posPregFin = preguntas.size - 1
        val posRespFin = respuestas.size - 1

        return when {
            contadorPregunta == 0 -> {
                ValidacionesGuiaModel(
                    message = "Ya no tienes preguntas anteriores",
                    contadorPregunta = contadorPregunta,
                    estadoPreguntasRespuestas = EstadoPreguntasRespuestas(preguntas, respuestas),
                )
            }

            contadorPregunta > posPregFin && contadorPregunta > posRespFin && editable.isEmpty() -> {
                val contador = contadorPregunta - 1

                val validacionesGuiaModel =
                    setPintarTextosUseCase(isEtPregunta = true, preguntas, respuestas, contador)

                val responseValGuiaModel: ValidacionesGuiaModel = validacionesGuiaModel.copy(
                    estadoUI = validacionesGuiaModel.estadoUI.copy(
                        isThereMoreAsks = true
                    )
                )

                return responseValGuiaModel
            }

            editable.toString().isEmpty() || (editable.toString()
                .isNotEmpty() && contadorPregunta > posPregFin) -> {
                ValidacionesGuiaModel(
                    message = "Asegurate de llenar pregunta y respuesta",
                    contadorPregunta = contadorPregunta,
                    estadoPreguntasRespuestas = EstadoPreguntasRespuestas(preguntas, respuestas),
                )
            }

            else -> {
                val responseSpanPalabra = setSpanPalabraUseCase(editable)
                val responseEtiquetaEditable =
                    setColocarEtiquetasUseCase(responseSpanPalabra.editable)

                if (isEtPregunta) {
                    preguntas[contadorPregunta] = responseEtiquetaEditable.toString()
                } else {
                    if (contadorPregunta > posRespFin) {
                        respuestas.add(contadorPregunta, responseEtiquetaEditable.toString())
                    } else {
                        respuestas[contadorPregunta] = responseEtiquetaEditable.toString()
                    }
                }

                val contador = contadorPregunta - 1

                val validacionesGuiaModel =
                    setPintarTextosUseCase(isEtPregunta = true, preguntas, respuestas, contador)

                val responseValGuiaModel: ValidacionesGuiaModel = validacionesGuiaModel.copy(
                    estadoUI = validacionesGuiaModel.estadoUI.copy(
                        isThereMoreAsks = true
                    )
                )

                return responseValGuiaModel
            }
        }
    }
}

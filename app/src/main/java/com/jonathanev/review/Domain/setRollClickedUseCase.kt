package com.jonathanev.review.Domain

import android.text.Editable
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import javax.inject.Inject

class setRollClickedUseCase @Inject constructor(
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
        val responseSpanPalabra = setSpanPalabraUseCase(editable)
        val responseEtiquetaEditable = setColocarEtiquetasUseCase(responseSpanPalabra.editable)

        return when {
            responseEtiquetaEditable.isEmpty() ->
                ValidacionesGuiaModel(
                    message = "Asegurate de llenar pregunta y respuesta",
                )

            else ->
                if (isEtPregunta) {
                    if ((contadorPregunta + 1) > respuestas.size) {
                        preguntas.add(contadorPregunta, responseEtiquetaEditable.toString())

                        ValidacionesGuiaModel(
                            estadoUI = EstadoUI(
                                isUpdatedAskAns = true,
                                isClearText = true,
                                isShowQuitColor = true,
                                isShowSelColor = true,
                                isEtPregunta = true
                            )
                        )
                    } else {
                        preguntas[contadorPregunta] = responseEtiquetaEditable.toString()
                        val responsePintarTextos = setPintarTextosUseCase(
                            false, // Get answer
                            preguntas,
                            respuestas,
                            contadorPregunta
                        )

                        responsePintarTextos.copy(
                            responseSpanPalabra = responseSpanPalabra,
                            estadoUI = responsePintarTextos.estadoUI.copy(
                                isEtPregunta = true
                            )
                        )
                    }
                } else {
                    if ((contadorPregunta + 1) > respuestas.size) {
                        respuestas.add(contadorPregunta, responseEtiquetaEditable.toString())

                        val responsePintarTextos = setPintarTextosUseCase(
                            true, // Get question
                            preguntas,
                            respuestas,
                            contadorPregunta
                        )

                        responsePintarTextos.copy(
                            responseSpanPalabra = responseSpanPalabra,
                            estadoUI = responsePintarTextos.estadoUI.copy(
                                isEtPregunta = false
                            )
                        )
                    } else {
                        respuestas[contadorPregunta] = responseEtiquetaEditable.toString()
                        val responsePintarTextos = setPintarTextosUseCase(
                            true, // Get question
                            preguntas,
                            respuestas,
                            contadorPregunta
                        )

                        responsePintarTextos.copy(
                            responseSpanPalabra = responseSpanPalabra,
                            estadoUI = responsePintarTextos.estadoUI.copy(
                                isEtPregunta = false
                            )
                        )
                    }
                }
        }
    }
}
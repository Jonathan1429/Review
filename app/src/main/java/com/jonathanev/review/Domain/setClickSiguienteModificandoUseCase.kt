package com.jonathanev.review.Domain

import android.text.Editable
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import javax.inject.Inject

class setClickSiguienteModificandoUseCase @Inject constructor(
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
        val responseSpanPalabra = setSpanPalabraUseCase(editable)
        val responseEtiquetaEditable = setColocarEtiquetasUseCase(responseSpanPalabra.editable)

        return when {
            responseEtiquetaEditable.toString().isEmpty() || (responseEtiquetaEditable.toString()
                .isNotEmpty() && contadorPregunta > posPregFin) -> {
                ValidacionesGuiaModel(
                    message = "Asegurate de llenar pregunta y respuesta",
                )
            }

            else -> {
                // Label pregunta
                if (isEtPregunta) {
                    preguntas[contadorPregunta] = responseEtiquetaEditable.toString()
                } else {
                    if (contadorPregunta > posRespFin) {
                        respuestas.add(contadorPregunta, responseEtiquetaEditable.toString())
                    } else {
                        respuestas[contadorPregunta] = responseEtiquetaEditable.toString()
                    }
                }

                val contador = contadorPregunta + 1
                // Si hay más preguntas pinta lo siguiente.
                if (contador <= posPregFin) {
                    // Pintamos texto o regresamos la pregunta
                    val validacionesguiaGuiaModel =
                        setPintarTextosUseCase(isEtPregunta = true, preguntas, respuestas, contador)

                    val responseValGuiaModel: ValidacionesGuiaModel =
                        validacionesguiaGuiaModel.copy(
                            responseSpanPalabra = responseSpanPalabra,
                            estadoUI = validacionesguiaGuiaModel.estadoUI.copy(isThereMoreAsks = true)
                        )

                    return responseValGuiaModel


                } else {
                    // Si no hay más preguntas.
                    ValidacionesGuiaModel(
                        responseSpanPalabra = responseSpanPalabra,
                        estadoUI = EstadoUI(
                            isUpdatedAskAns = true,
                            isClearText = true,
                            isShowQuitColor = true,
                            isShowSelColor = true
                        )
                    )
                }
            }
        }
    }
}
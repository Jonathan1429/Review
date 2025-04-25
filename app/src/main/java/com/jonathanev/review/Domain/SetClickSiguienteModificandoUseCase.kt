package com.jonathanev.review.Domain

import android.text.Editable
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import javax.inject.Inject

class SetClickSiguienteModificandoUseCase @Inject constructor(
    private val setSpanPalabraUseCase: SetSpanPalabraUseCase,
    private val setColocarEtiquetasUseCase: SetColocarEtiquetasUseCase,
    private val setPintarTextosUseCase: SetPintarTextosUseCase
) {
    operator fun invoke(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        editable: Editable,
        isEtPregunta: Boolean
    ): ValidacionesGuiaModel {
        return when {
            editable.isEmpty() -> {
                ValidacionesGuiaModel(
                    message = "Asegurate de llenar pregunta y respuesta",
                )
            }

            else -> {
                val responseSpanPalabra = setSpanPalabraUseCase(editable)
                val responseEtiquetaEditable =
                    setColocarEtiquetasUseCase(responseSpanPalabra.editable)

                val posPregFin = preguntas.size - 1
                val posRespFin = respuestas.size - 1

                if (contadorPregunta <= posRespFin) {
                    if (isEtPregunta) {
                        preguntas[contadorPregunta] = responseEtiquetaEditable.toString()
                    } else {
                        respuestas[contadorPregunta] = responseEtiquetaEditable.toString()
                    }
                } else {
                    respuestas.add(contadorPregunta, responseEtiquetaEditable.toString())
                }

                val conSiguientePregunta = contadorPregunta + 1
                // Si hay más preguntas pinta lo siguiente.
                if (conSiguientePregunta <= posPregFin) {
                    // Pintamos texto o regresamos la pregunta
                    val validacionesguiaGuiaModel =
                        setPintarTextosUseCase(
                            isEtPregunta = true,
                            preguntas,
                            respuestas,
                            conSiguientePregunta
                        )

                    validacionesguiaGuiaModel.copy(
                        responseSpanPalabra = responseSpanPalabra,
                        estadoUI = validacionesguiaGuiaModel.estadoUI.copy(isThereMoreAsks = true)
                    )
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
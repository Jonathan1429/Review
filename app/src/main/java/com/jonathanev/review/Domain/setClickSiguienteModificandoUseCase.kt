package com.jonathanev.review.Domain

import android.text.Editable
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
                    contadorPregunta = contadorPregunta,
                    preguntas = preguntas,
                    respuestas = respuestas
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
                    // Pintamos el texto en la pregunta actual
                    val setPintarTextosUseCase =
                        setPintarTextosUseCase(isEtPregunta = true, preguntas, respuestas, contador)

                    // Si no es una imagen entra
                    if (!setPintarTextosUseCase.builder.isNullOrEmpty()) {
                        ValidacionesGuiaModel(
                            contadorPregunta = contador,
                            responseSpanPalabra = responseSpanPalabra,
                            isUpdatedAskAns = true,
                            isShowQuitColor = true,
                            isShowSelColor = true,
                            isThereMoreAsks = true,
                            builder = setPintarTextosUseCase.builder,
                            preguntas = preguntas,
                            respuestas = respuestas
                        )
                    } else {
                        // Si es una imagen entra
                        ValidacionesGuiaModel(
                            contadorPregunta = contador,
                            isUpdatedAskAns = true,
                            isShowImage = true,
                            isShowCancelar = true,
                            isThereMoreAsks = true,
                            textImgEcrypted = setPintarTextosUseCase.textImgEcrypted,
                            textImgUnencrypted = setPintarTextosUseCase.textImgUnencrypted,
                            preguntas = preguntas,
                            respuestas = respuestas
                        )
                    }
                } else {
                    // Si no hay más preguntas.
                    ValidacionesGuiaModel(
                        contadorPregunta = contador,
                        responseSpanPalabra = responseSpanPalabra,
                        isUpdatedAskAns = true,
                        isClearText = true,
                        isShowQuitColor = true,
                        isShowSelColor = true,
                        preguntas = preguntas,
                        respuestas = respuestas
                    )
                }
            }
        }
    }
}
package com.jonathanev.review.Domain

import android.text.Editable
import javax.inject.Inject
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel

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
                    isUpdatedAskAns = false,
                    preguntas = preguntas,
                    respuestas = respuestas
                )
            }

            contadorPregunta > posPregFin && contadorPregunta > posRespFin && editable.isEmpty() -> {
                val contador = contadorPregunta - 1

                val setPintarTextosUseCase =
                    setPintarTextosUseCase(isEtPregunta = true, preguntas, respuestas, contador)

                // Si no es una imagen entra
                if (!setPintarTextosUseCase.builder.isNullOrEmpty()) {
                    ValidacionesGuiaModel(
                        contadorPregunta = contador,
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
            }

            editable.toString().isEmpty() || (editable.toString()
                .isNotEmpty() && contadorPregunta > posPregFin) -> {
                ValidacionesGuiaModel(
                    message = "Asegurate de llenar pregunta y respuesta",
                    contadorPregunta = contadorPregunta,
                    isUpdatedAskAns = false,
                    preguntas = preguntas,
                    respuestas = respuestas
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

                val setPintarTextosUseCase =
                    setPintarTextosUseCase(isEtPregunta = true, preguntas, respuestas, contador)

                // Si no es una imagen entra
                if (!setPintarTextosUseCase.builder.isNullOrEmpty()) {
                    ValidacionesGuiaModel(
                        contadorPregunta = contador,
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
            }
        }
    }
}

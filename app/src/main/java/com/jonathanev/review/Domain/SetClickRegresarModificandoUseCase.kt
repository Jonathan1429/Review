package com.jonathanev.review.Domain

import android.text.Editable
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import javax.inject.Inject

class SetClickRegresarModificandoUseCase @Inject constructor(
    private val setSpanPalabraUseCase: SetSpanPalabraUseCase,
    private val setColocarEtiquetasUseCase: SetColocarEtiquetasUseCase,
    private val setPintarTextosUseCase: SetPintarTextosUseCase
) {
    operator fun invoke(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        editable: Editable,
        isEtPregunta: Boolean,
        ruta: String
    ): ValidacionesGuiaModel {
        val posPregFin = preguntas.size - 1
        val posRespFin = respuestas.size - 1

        return when {
            contadorPregunta == 0 -> {
                ValidacionesGuiaModel(
                    message = "Ya no tienes preguntas anteriores",
                )
            }
            editable.isEmpty() && !isEtPregunta || isEtPregunta && contadorPregunta <= posPregFin && editable.isEmpty() || contadorPregunta > posPregFin && editable.isNotEmpty() -> {
                ValidacionesGuiaModel(
                    message = "Asegurate de llenar pregunta y respuesta",
                )
            }
            else -> {
                if(editable.isEmpty()){
                    // Pintar texto anterior
                    val validacionesGuiaModel =
                        setPintarTextosUseCase(
                            isEtPregunta = true,
                            preguntas,
                            respuestas,
                            contadorPregunta - 1,
                            ruta
                        )

                    validacionesGuiaModel.copy(
                        estadoUI = validacionesGuiaModel.estadoUI.copy(
                            isThereMoreAsks = true
                        )
                    )
                } else{
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
                        setPintarTextosUseCase(isEtPregunta = true, preguntas, respuestas, contador, ruta)

                    validacionesGuiaModel.copy(
                        responseSpanPalabra = responseSpanPalabra,
                        estadoUI = validacionesGuiaModel.estadoUI.copy(
                            isThereMoreAsks = true
                        )
                    )
                }
            }
        }
    }
}

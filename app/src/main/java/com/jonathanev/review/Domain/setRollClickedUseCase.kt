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

            /*if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                if ((contadorPregunta + 1) > respuestas.size) {
                    binding!!.lblPregResp.text = "Respuesta"
                    // binding!!.lblPregResp.text = "Respuesta"
                    preguntas.add(contadorPregunta, editableEditquetas.toString())
                    binding!!.etPregResp.setText("")
                    binding!!.ivImagen.visibility = View.GONE
                    binding!!.tilContenidoPregResp.visibility = View.VISIBLE

                    binding!!.imgvCancelar.visibility = View.GONE
                    binding!!.imgvQuitColor.visibility = View.VISIBLE
                    binding!!.imgvSelColor.visibility = View.VISIBLE
                } else {
                    binding!!.lblPregResp.text = "Respuesta"
                    preguntas[contadorPregunta] = editableEditquetas.toString()
                    pintarTexto(contadorPregunta)
                    // binding!!.lblPregResp.text = "Respuesta"
                }
                girarCardView()
            } else {
                if ((contadorPregunta + 1) > respuestas.size) {
                    binding!!.lblPregResp.text = "Pregunta"
                    respuestas.add(contadorPregunta, editableEditquetas.toString())
                    pintarTexto(contadorPregunta)
                    // binding!!.lblPregResp.text = "Pregunta"
                } else {
                    binding!!.lblPregResp.text = "Pregunta"
                    respuestas[contadorPregunta] = editableEditquetas.toString()
                    pintarTexto(contadorPregunta)
                    // binding!!.lblPregResp.text = "Pregunta"
                }
                girarCardView()
            }

            activityCuestionarioViewModel.clickedRoll()
            posColorFinal = -1
            posColorInicial = -1
            colorPintarPalabra = 0
        } else {
            Toast.makeText(
                applicationContext,
                "Asegurate de no dejar ningun campo vacio",
                Toast.LENGTH_SHORT
            ).show()

            Log.i("Crear pregunta: ", "Asegurate de no dejar ningun campo vacio")
        }*/
        }
    }
}
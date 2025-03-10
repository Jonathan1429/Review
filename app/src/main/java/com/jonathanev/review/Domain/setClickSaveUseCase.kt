package com.jonathanev.review.Domain

import android.text.Editable
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import javax.inject.Inject

class setClickSaveUseCase @Inject constructor(
    private val setSpanPalabraUseCase: setSpanPalabraUseCase,
    private val setColocarEtiquetasUseCase: setColocarEtiquetasUseCase,
    private val setCrearXmlUseCase: setCrearXmlUseCase
) {
    operator fun invoke(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        editable: Editable,
        nombreArchivo: String,
        isEtPregunta: Boolean,
        didTheGuideAlreadyExist: Boolean,
        ruta: String
    ): ValidacionesGuiaModel {
        val posPregFin = preguntas.size - 1
        val posRespFin = respuestas.size - 1
        val responseSpanPalabra = setSpanPalabraUseCase(editable)
        val responseEtiquetaEditable = setColocarEtiquetasUseCase(responseSpanPalabra.editable)

        return when {
            responseSpanPalabra.editable.isEmpty() || isEtPregunta && posPregFin != posRespFin -> {
                if (isEtPregunta && posPregFin > -1) {
                    setCrearXmlUseCase(nombreArchivo, preguntas, respuestas, didTheGuideAlreadyExist, ruta)

                    /*// Si el archivo no existe
                    if (!didTheGuideAlreadyExist) {
                        setCrearXmlUseCase(nombreArchivo, preguntas, respuestas)
                    } else { // Si el archivo ya existe
                        setBorrarCrearXmlUseCase(nombreArchivo, preguntas, respuestas, ruta)
                    }*/
                    // Se tiene que guardar la guia y crear el archivo
                    //setCrearXmlUseCase(nombreArchivo, preguntas, respuestas)
                } else {
                    ValidacionesGuiaModel(
                        message = "Asegurate de llenar pregunta y respuesta",
                        estadoUI = EstadoUI(isUpdatedAskAns = false)
                    )
                }
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

                setCrearXmlUseCase(nombreArchivo, preguntas, respuestas, didTheGuideAlreadyExist, ruta)

                /*// Si el archivo no existe
                if (!didTheGuideAlreadyExist) {
                    setCrearXmlUseCase(nombreArchivo, preguntas, respuestas)
                } else { // Si el archivo ya existe
                    setBorrarCrearXmlUseCase(nombreArchivo, preguntas, respuestas, ruta)
                }*/
            }
        }
    }
}
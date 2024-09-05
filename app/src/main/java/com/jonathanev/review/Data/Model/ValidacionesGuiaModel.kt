package com.jonathanev.review.Data.Model

import android.text.Editable

// Clase de datos para encapsular el resultado
data class ValidacionesGuiaModel(
    val message: String = "",
    val responseSpanPalabra: SpanPalabraModel? = null,
    val textImgEcrypted: String = "",
    val textImgUnencrypted: String = "",
    val contadorPregunta: Int,
    val isUpdatedAskAns: Boolean = false,
    val isClearText: Boolean = false,
    val isShowImage: Boolean = false,
    val isShowCancelar: Boolean = false,
    val isShowQuitColor: Boolean = false,
    val isShowSelColor: Boolean = false,
    val isThereMoreAsks: Boolean = false,
    val builder: Editable? = null,
    val preguntas: ArrayList<String>,
    val respuestas: ArrayList<String>
)
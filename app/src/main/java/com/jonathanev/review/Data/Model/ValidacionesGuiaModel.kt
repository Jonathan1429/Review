package com.jonathanev.review.Data.Model

import android.text.Editable

data class ValidacionesGuiaModel(
    val message: String = "",
    val responseSpanPalabra: SpanPalabraModel? = null,
    val contadorPregunta: Int = 0,
    val estadoPreguntasRespuestas: EstadoPreguntasRespuestas = EstadoPreguntasRespuestas(),
    val estadoImagen: EstadoImagen = EstadoImagen(),
    val estadoUI: EstadoUI = EstadoUI(),
    val responseGuia: ResponseGuia = ResponseGuia(),
    val builder: Editable? = null
)

data class ResponseGuia(
    val rutaGuiaEstudio: String = "",

)
data class EstadoPreguntasRespuestas(
    val preguntas: ArrayList<String> = arrayListOf(),
    val respuestas: ArrayList<String> = arrayListOf()
)

data class EstadoImagen(
    val textImgEcrypted: String = "",
    val textImgUnencrypted: String = ""
)

data class EstadoUI(
    val isUpdatedAskAns: Boolean = false,
    val isCreatedGuia: Boolean = false,
    val isClearText: Boolean = false,
    val isShowImage: Boolean = false,
    val isShowCancelar: Boolean = false,
    val isShowQuitColor: Boolean = false,
    val isShowSelColor: Boolean = false,
    val isThereMoreAsks: Boolean = false,
)
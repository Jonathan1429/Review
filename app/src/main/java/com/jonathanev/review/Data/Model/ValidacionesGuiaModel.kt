package com.jonathanev.review.Data.Model

import com.jonathanev.review.Data.Model.prueba.QuestionContent

data class ValidacionesGuiaModel(
    val responseSpanPalabra: SpanPalabraModel? = null,
    val estadoUI: EstadoUI = EstadoUI(),
    val responseGuia: ResponseGuia = ResponseGuia(),
)

data class ResponseGuia(
    val rutaGuiaEstudio: String = ""
)

data class EstadoUI(
    val shouldFlip: Boolean = false,
    val message: String = "",
    val content: QuestionContent = QuestionContent.None,
    val internalRules: InternalRules = InternalRules(),
){
    val showImage get() = content is QuestionContent.Image
    val showTextInput get() = content is QuestionContent.Text
}

data class InternalRules(
    val isUpdatedAskAns: Boolean = false,
    val isCreatedGuia: Boolean = false,
    val isClearText: Boolean = false,
    val isShowCancelar: Boolean = false,
    val isShowQuitColor: Boolean = false,
    val isShowSelColor: Boolean = false,
    val isThereMoreAsks: Boolean = true,
    val isEtPregunta: Boolean = false,
    val addMoreQuestions: Boolean = false
)
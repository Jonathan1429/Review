package com.jonathanev.review.Data.Model.prueba

sealed class UIStopEvent {
    //data class PaintTextColors(val text: String) : UiStopEvent()
    data class ShowMessage(val text: String) : UIStopEvent()
    data class NotQuestionBefore(val text: String): UIStopEvent()
    data class DeleteGuideSuccess(val text: String): UIStopEvent()
    data class AddMoreQuestions(val text: String): UIStopEvent()
    data class NotQuestionNext(val text: String): UIStopEvent()
    data class RestartGuide(val text: String): UIStopEvent()
}
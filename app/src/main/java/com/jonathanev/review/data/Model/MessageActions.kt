package com.jonathanev.review.data.Model

sealed class MessageActions() {
    data object FieldEmpty: MessageActions()
    data object AddMoreQuestions: MessageActions()
    data object Continue: MessageActions()
    data object WithoutQuestionsBefore: MessageActions()
    //data object WithoutActions: MessageActions()
}

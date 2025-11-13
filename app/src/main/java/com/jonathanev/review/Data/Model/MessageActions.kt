package com.jonathanev.review.Data.Model

sealed class MessageActions() {
    data object FieldEmpty: MessageActions()
    data object AddMoreQuestions: MessageActions()
    data object Continue: MessageActions()
    data object WithoutQuestionsBefore: MessageActions()
    //data object WithoutActions: MessageActions()
}

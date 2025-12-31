package com.jonathanev.review.presentation.state

import com.jonathanev.review.presentation.model.QuestionItemDomain

sealed class ResponseDomain {
    data object Empty : ResponseDomain()
    data class Filled(val item: QuestionItemDomain) : ResponseDomain()
}

data class QAItemDomain(
    val question: ResponseDomain = ResponseDomain.Empty,
    val answer: ResponseDomain = ResponseDomain.Empty
)

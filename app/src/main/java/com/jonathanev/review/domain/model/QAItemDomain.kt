package com.jonathanev.review.domain.model

data class QAItemDomain(
    val question: ResponseDomain = ResponseDomain.Empty,
    val answer: ResponseDomain = ResponseDomain.Empty
)

sealed class ResponseDomain {
    data object Empty : ResponseDomain()
    data class Filled(val item: QuestionItemDomain) : ResponseDomain()
}
package com.jonathanev.review.domain.model

data class QAItemDomain(
    val question: QuestionItemDomain,
    val answer: QuestionItemDomain
)
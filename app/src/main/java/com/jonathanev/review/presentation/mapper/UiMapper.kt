package com.jonathanev.review.presentation.mapper

import com.jonathanev.review.domain.model.PreviewQuestionDomain
import com.jonathanev.review.presentation.model.PreviewQuestionUi
import com.jonathanev.review.domain.model.ColorRangeDomain
import com.jonathanev.review.presentation.model.ColorRangeUi
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.presentation.model.QuestionItemUi

fun PreviewQuestionDomain.toUi(): PreviewQuestionUi {
    return PreviewQuestionUi(
        this.question.toUi(),
        noImages = this.noImages.toString()
    )
}

fun QuestionItemDomain.toUi(): QuestionItemUi {
    return QuestionItemUi(
        content = content.map { it.toUi() }
    )
}

fun QuestionItemUi.toDomain(): QuestionItemDomain {
    return QuestionItemDomain(
        content = content.map { it.toDomain() }
    )
}

fun QuestionContentDomain.Text.toUi(): QuestionContentUi.Text {
    return QuestionContentUi.Text(this.text, this.colorRangeDomains.map { it.toUi() })
}

fun QuestionContentDomain.Image.toUi(): QuestionContentUi.Image {
    return QuestionContentUi.Image(this.uri, this.nameFile)
}

fun QuestionContentDomain.toUi(): QuestionContentUi {
    return when (this) {
        is QuestionContentDomain.Text -> QuestionContentUi.Text(
            text = this.text,
            colorRanges = this.colorRangeDomains.map { it.toUi() }
        )

        is QuestionContentDomain.Image -> QuestionContentUi.Image(
            uri = this.uri,
            nameFile = this.nameFile
        )
    }
}

fun QuestionContentUi.toDomain(): QuestionContentDomain {
    return when (this) {
        is QuestionContentUi.None ->  error("Estado invalido: None no puede pasar a domain")
        is QuestionContentUi.Text -> QuestionContentDomain.Text(
            text = this.text,
            colorRangeDomains = this.colorRanges.map { it.toDomain() }
        )

        is QuestionContentUi.Image -> QuestionContentDomain.Image(
            uri = this.uri,
            nameFile = this.nameFile
        )
    }
}

fun ColorRangeDomain.toUi(): ColorRangeUi {
    return ColorRangeUi(start = start, end = end, color = color)
}

fun ColorRangeUi.toDomain(): ColorRangeDomain {
    return ColorRangeDomain(start = start, end = end, color = color)
}
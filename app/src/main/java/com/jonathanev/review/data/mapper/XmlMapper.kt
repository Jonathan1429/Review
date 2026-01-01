package com.jonathanev.review.data.mapper

import com.jonathanev.review.domain.model.ContentType
import com.jonathanev.review.domain.model.QAType
import com.jonathanev.review.data.model.ColorRangeXml
import com.jonathanev.review.data.model.QAItemXml
import com.jonathanev.review.data.model.QuestionContentXml
import com.jonathanev.review.data.model.QuestionItemXml
import com.jonathanev.review.data.model.ResponseXml
import com.jonathanev.review.domain.model.ColorRangeDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.data.xml.XmlTagsV2

fun toXmlContent(type: ContentType): String =
    when (type) {
        ContentType.TEXT -> XmlTagsV2.TEXTO
        ContentType.IMAGE -> XmlTagsV2.IMAGEN
    }

fun toXmlQA(type: QAType): String =
    when (type) {
        QAType.QUESTION -> XmlTagsV2.QUESTION
        QAType.ANSWER -> XmlTagsV2.ANSWER
    }

fun QAItemXml.toDomain(): QAItemDomain {
    return QAItemDomain(this.question.toDomain(), this.answer.toDomain())
}

fun ResponseXml.toDomain(): ResponseDomain {
    return when (this) {
        ResponseXml.Empty -> ResponseDomain.Empty
        is ResponseXml.Filled -> ResponseDomain.Filled(this.item.toDomain())
    }
}

fun QuestionItemXml.toDomain(): QuestionItemDomain {
    return QuestionItemDomain(this.content.map { it.toDomain() })
}

fun QuestionContentXml.toDomain(): QuestionContentDomain {
    return when (this) {
        is QuestionContentXml.Image -> QuestionContentDomain.Image(this.uri, this.nameFile)
        QuestionContentXml.None -> QuestionContentDomain.None
        is QuestionContentXml.Text -> QuestionContentDomain.Text(
            this.text,
            this.colorRangeXml.map { it.toDomain() })
    }
}

fun ColorRangeXml.toDomain(): ColorRangeDomain {
    return ColorRangeDomain(this.start, this.end, this.color)
}

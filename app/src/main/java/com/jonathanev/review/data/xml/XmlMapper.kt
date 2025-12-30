package com.jonathanev.review.data.xml

import com.jonathanev.review.Domain.model.ContentType
import com.jonathanev.review.Domain.model.QAType

object XmlMapper {
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
}


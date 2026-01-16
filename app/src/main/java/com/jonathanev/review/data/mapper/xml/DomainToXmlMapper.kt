package com.jonathanev.review.data.mapper.xml

import com.jonathanev.review.data.xml.XmlTagsV2
import com.jonathanev.review.domain.model.ContentType
import com.jonathanev.review.domain.model.QAType

fun ContentType.toTagXml(): String =
    when (this) {
        ContentType.TEXT -> XmlTagsV2.TEXTO
        ContentType.IMAGE -> XmlTagsV2.IMAGEN
    }

fun QAType.toTagXml(): String =
    when (this) {
        QAType.QUESTION -> XmlTagsV2.QUESTION
        QAType.ANSWER -> XmlTagsV2.ANSWER
    }
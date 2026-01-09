package com.jonathanev.review.domain.repository

import com.jonathanev.review.data.Extensions

object FileNamingRules {
    fun buildXmlFileName(name: String): String {
        return "$name${Extensions.POINT_XML_EXTENSION}"
    }

    fun buildPngFileName(name: String): String {
        return "$name${Extensions.POINT_PNG_EXTENSION}"
    }
}

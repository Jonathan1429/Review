package com.jonathanev.review.domain.repository

object FileNamingRules {
    const val XML_EXTENSION = ".xml"

    fun buildXmlFileName(name: String): String {
        return "$name$XML_EXTENSION"
    }
}

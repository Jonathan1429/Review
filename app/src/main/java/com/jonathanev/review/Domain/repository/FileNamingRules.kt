package com.jonathanev.review.Domain.repository

object FileNamingRules {
    private const val XML_EXTENSION = "xml"

    fun buildXmlFileName(name: String): String {
        return "$name.$XML_EXTENSION"
    }
}

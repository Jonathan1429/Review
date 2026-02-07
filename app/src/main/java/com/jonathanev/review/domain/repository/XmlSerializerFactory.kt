package com.jonathanev.review.domain.repository

import org.xmlpull.v1.XmlSerializer

interface XmlSerializerFactory {
    fun create(): XmlSerializer
}
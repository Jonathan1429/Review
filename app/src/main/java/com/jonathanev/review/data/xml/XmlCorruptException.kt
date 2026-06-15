package com.jonathanev.review.data.xml

import org.xml.sax.SAXException
import java.util.Locale

//class XmlCorruptException(message: String) : SAXException(message) {
class XmlCorruptException() : SAXException() {
    override fun getLocalizedMessage(): String {
        val languageMovil = Locale.getDefault().language

        return if (languageMovil == "es") {
            "El archivo XML está dañado o no tiene la estructura correcta"
        } else {
            super.message ?: "The XML file is corrupt."
        }
    }
}
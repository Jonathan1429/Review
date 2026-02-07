package com.jonathanev.review.data.infraestructure

import android.util.Xml
import com.jonathanev.review.domain.repository.XmlSerializerFactory
import org.xmlpull.v1.XmlSerializer
import javax.inject.Inject

class AndroidXmlSerializerFactory @Inject constructor(): XmlSerializerFactory {
    override fun create(): XmlSerializer = Xml.newSerializer()
}
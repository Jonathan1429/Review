package com.jonathanev.review.data.filesystem

import com.jonathanev.review.domain.repository.XmlSerializerFactory
import org.kxml2.io.KXmlSerializer
import org.xmlpull.v1.XmlSerializer

class FakeXmlSerializerFactory : XmlSerializerFactory {
    override fun create(): XmlSerializer {
        return KXmlSerializer() // org.kxml2:kxml2
    }
}

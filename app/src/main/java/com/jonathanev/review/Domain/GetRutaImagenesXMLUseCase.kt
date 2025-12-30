package com.jonathanev.review.Domain

import com.jonathanev.review.Domain.repository.FileNamingRules
import com.jonathanev.review.data.xml.Versions
import java.io.File
import javax.inject.Inject

class GetRutaImagenesXMLUseCase @Inject constructor() {
    operator fun invoke(file: File, version: String): File {
        val basePath = if (version == Versions.VERSION2){
            file.toString().replace(FileNamingRules.XML_EXTENSION, "")
        } else {
            file.parent ?: ""
        }

        val imagesFolder = File(basePath.replace("guias", "imagenes"))
        return imagesFolder
    }
}
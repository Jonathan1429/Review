package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants.VERSION2
import com.jonathanev.review.Domain.repository.FileNamingRules
import java.io.File
import javax.inject.Inject

class GetRutaImagenesXMLUseCase @Inject constructor() {
    operator fun invoke(file: File, version: String): File {
        val basePath = if (version == VERSION2){
            file.toString().replace(FileNamingRules.XML_EXTENSION, "")
        } else {
            file.parent ?: ""
        }

        val imagesFolder = File(basePath.replace("guias", "imagenes"))
        return imagesFolder
    }
}
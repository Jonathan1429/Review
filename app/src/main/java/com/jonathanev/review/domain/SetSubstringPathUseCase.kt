package com.jonathanev.review.domain

import com.jonathanev.review.data.xml.Versions
import javax.inject.Inject

class SetSubstringPathUseCase @Inject constructor() {
    operator fun invoke(path: String, decoded: String = "", version: String, nameFile: String = ""): String{
        var newPath = ""
        newPath = if (version == Versions.VERSION1) {
            newPath = path.substringAfter("/")
            newPath = newPath.replace("guias", "imagenes")
            newPath = newPath.substringBeforeLast("/")
            val image = decoded.substringAfterLast("/")
            "$newPath/$image"
        } else {
            newPath = path.replace(".xml", "")
            newPath = newPath.replace("guias", "imagenes")
            "$newPath/$nameFile"
        }

        return newPath
    }
}
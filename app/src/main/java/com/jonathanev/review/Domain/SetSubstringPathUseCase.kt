package com.jonathanev.review.Domain

import javax.inject.Inject

class SetSubstringPathUseCase @Inject constructor() {
    operator fun invoke(path: String, decoded: String): String{
        var newPath = path.substringAfter("/")
        newPath = newPath.replace("guias", "imagenes")
        newPath = newPath.substringBeforeLast("/")
        val image = decoded.substringAfterLast("/")
        newPath = "$newPath/$image"
        return newPath
    }
}
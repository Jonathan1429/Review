package com.jonathanev.review.data.util

import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.domain.model.GuideVersion
import java.io.File
import javax.inject.Inject

class PathHandler @Inject constructor() {
    fun encrypt(texto: String, desplazamiento: Int = 23): String {
        val resultado = StringBuilder()

        for (caracter in texto) {
            if (caracter.isLetter()) {
                val base = if (caracter.isUpperCase()) 'A' else 'a'
                val letraCifrada =
                    ((caracter - base + desplazamiento) % 26 + base.code).toChar()
                resultado.append(letraCifrada)
            } else {
                resultado.append(caracter)
            }
        }

        return resultado.toString()
    }

    fun getSubstringPath(path: String, decoded: String = "", version: GuideVersion, nameFile: String = ""): String{
        var newPath = path
        val separator = File.separator

        newPath = if (version == GuideVersion.V1) {
            newPath = path.substringAfter(separator)
            newPath = newPath.replace(StorageFolders.GUIAS, StorageFolders.IMAGENES)
            newPath = newPath.substringBeforeLast(separator)
            val image = decoded.substringAfterLast(separator)
            "$newPath$separator$image"
        } else {
            newPath = newPath.replace(StorageFolders.GUIAS, StorageFolders.IMAGENES)
            "$newPath$separator$nameFile"
        }

        return newPath
    }
}
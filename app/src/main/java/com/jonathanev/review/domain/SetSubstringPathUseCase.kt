package com.jonathanev.review.domain

import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.data.xml.Versions
import javax.inject.Inject

class SetSubstringPathUseCase @Inject constructor() {
    operator fun invoke(path: String, decoded: String = "", version: String, nameFile: String = ""): String{
        var newPath = path
        newPath = if (version == Versions.VERSION1) {
            newPath = path.substringAfter("/")
            newPath = newPath.replace(StorageFolders.GUIAS, StorageFolders.IMAGENES)
            newPath = newPath.substringBeforeLast("/")
            val image = decoded.substringAfterLast("/")
            "$newPath/$image"
        } else {
            newPath = newPath.replace(StorageFolders.GUIAS, StorageFolders.IMAGENES)
            "$newPath/$nameFile"
        }

        return newPath
    }
}
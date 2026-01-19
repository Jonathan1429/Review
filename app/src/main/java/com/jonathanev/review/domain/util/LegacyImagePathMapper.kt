package com.jonathanev.review.domain.util

import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.data.xml.Versions
import javax.inject.Inject

class LegacyImagePathMapper @Inject constructor() {
    fun map(path: String, decoded: String = "", version: String, nameFile: String = ""): String {
        return if (version == Versions.VERSION1) {
            val base = path
                .substringAfter("/")
                .replace(StorageFolders.GUIAS, StorageFolders.IMAGENES)
                .substringBeforeLast("/")

            val image = decoded.substringAfterLast("/")
            "$base/$image"
        } else {
            val base = path.replace(StorageFolders.GUIAS, StorageFolders.IMAGENES)
            "$base/$nameFile"
        }
    }
}
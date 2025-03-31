package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.R
import java.io.File
import javax.inject.Inject

class getAllGuiasUseCase @Inject constructor(
    val getRandomGuiaImage: getRandomGuiaImage
) {
    operator fun invoke(file: File): List<GuiaModel> {
        val files = file.listFiles() ?: return emptyList()
        val image = getRandomGuiaImage()

        return files.map { archivo ->
            val isFolder = archivo.isDirectory
            GuiaModel(
                nombreGuia = archivo.name.replace(".xml", ""),
                imgGuia = if (isFolder) R.drawable.img_carpeta else image,
                carpeta = isFolder
            )
        }.sortedWith(compareBy<GuiaModel> { !it.carpeta }.thenBy { it.nombreGuia })
    }
}
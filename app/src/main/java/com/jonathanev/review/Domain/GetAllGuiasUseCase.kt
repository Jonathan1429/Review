package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.R
import java.io.File
import javax.inject.Inject

class GetAllGuiasUseCase @Inject constructor(
    //val getRandomGuiaImageUseCase: GetRandomGuiaImageUseCase
) {
    operator fun invoke(file: File): List<GuiaModel> {
        val files = file.listFiles() ?: return emptyList()
        //val image = getRandomGuiaImageUseCase()

        return files.map { archivo ->
            val isFolder = archivo.isDirectory
            GuiaModel(
                nombreGuia = archivo.name.replace(".xml", ""),
                //imgGuia = if (isFolder) R.drawable.img_carpeta else image,
                carpeta = isFolder
            )
        }.sortedWith(compareBy<GuiaModel> { !it.carpeta }.thenBy { it.nombreGuia })
    }
}
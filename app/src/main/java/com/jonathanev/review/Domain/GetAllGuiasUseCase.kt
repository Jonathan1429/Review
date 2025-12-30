package com.jonathanev.review.Domain

import com.jonathanev.review.data.Model.GuideModel
import java.io.File
import javax.inject.Inject

class GetAllGuiasUseCase @Inject constructor(
    //val getRandomGuiaImageUseCase: GetRandomGuiaImageUseCase
) {
    operator fun invoke(file: File): List<GuideModel> {
        val files = file.listFiles() ?: return emptyList()
        //val image = getRandomGuiaImageUseCase()

        return files.map { archivo ->
            //val isFolder = archivo.isDirectory
            GuideModel(
                nameGuide = archivo.name.replace(".xml", ""),
                description = ""
                //imgGuia = if (isFolder) R.drawable.img_carpeta else image,
            )
        }.sortedBy { it.nameGuide }

        //sortedWith(  compareBy<GuiaModel> { !it.carpeta }.thenBy { it.nombreGuia })
    }
}
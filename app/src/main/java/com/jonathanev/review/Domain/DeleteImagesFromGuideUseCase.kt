package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants.GUIAS
import com.jonathanev.review.Core.Constants.IMAGENES
import com.jonathanev.review.Core.Constants.VERSION2
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import java.io.File
import javax.inject.Inject

class DeleteImagesFromGuideUseCase @Inject constructor() {
    operator fun invoke(
        version: String,
        currentGuide: File,
        listImages: List<QuestionContent.Image>
    ): Boolean {
        if (version == VERSION2){
            var pathImages = File(currentGuide.path.replace(".xml", ""))
            pathImages = File(pathImages.path.replace(GUIAS, IMAGENES))
            if (!pathImages.exists()){
                return true
            }

            return pathImages.deleteRecursively()
        } else { // Version 1
            var pathImages = File(currentGuide.toString().substringBeforeLast("/"))
            pathImages = File(pathImages.path.replace(GUIAS, IMAGENES))

            listImages.forEach { image ->
                val pathImage = File(pathImages, image.nameFile)
                pathImage.delete()
            }

            return true
        }
    }
}
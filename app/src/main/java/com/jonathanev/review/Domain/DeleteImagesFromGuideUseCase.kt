package com.jonathanev.review.Domain

import com.jonathanev.review.data.Model.prueba.QuestionContent
import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.data.xml.Versions
import java.io.File
import javax.inject.Inject

class DeleteImagesFromGuideUseCase @Inject constructor() {
    operator fun invoke(
        version: String,
        currentGuide: File,
        listImages: List<QuestionContent.Image>
    ): Boolean {
        if (version == Versions.VERSION2){
            var pathImages = File(currentGuide.path.replace(".xml", ""))
            pathImages = File(pathImages.path.replace(StorageFolders.GUIAS, StorageFolders.IMAGENES))
            if (!pathImages.exists()){
                return true
            }

            return pathImages.deleteRecursively()
        } else { // Version 1
            var pathImages = File(currentGuide.toString().substringBeforeLast("/"))
            pathImages = File(pathImages.path.replace(StorageFolders.GUIAS, StorageFolders.IMAGENES))

            listImages.forEach { image ->
                val pathImage = File(pathImages, image.nameFile)
                pathImage.delete()
            }

            return true
        }
    }
}
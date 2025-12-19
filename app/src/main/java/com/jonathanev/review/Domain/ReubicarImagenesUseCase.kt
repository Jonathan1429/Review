package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants.GUIAS
import com.jonathanev.review.Core.Constants.IMAGENES
import com.jonathanev.review.Core.Constants.VERSION2
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.provider.FilePathsProvider
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject

class ReubicarImagenesUseCase @Inject constructor(
    private val getVersionUseCase: GetVersionUseCase,
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke(
        currentPath: File,
        fileName: String,
        preguntas: List<QuestionItem>,
        respuestas: List<QuestionItem>
    ) {
        val version = getVersionUseCase.invoke(currentPath)
        val oldPathImages = File(currentPath.path.replace(GUIAS, IMAGENES).replace(".xml", ""))

        if (version == VERSION2){
            val newPathImages = File(oldPathImages.parent, fileName)
            oldPathImages.renameTo(newPathImages)
        } else { // Version 1
            val newPathImages = File(oldPathImages.parent, fileName)
            newPathImages.mkdir()

            val listImages = (preguntas + respuestas)
                .flatMap { it.content }
                .filterIsInstance<QuestionContent.Image>()

            listImages.forEach { image ->
                val source = File(image.uri)
                if (source.exists()){
                    val noImage = image.uri.substringAfterLast("/")
                    val newPathFile = filePathsProvider.buildImage(newPathImages, noImage)

                    Files.copy(
                        Paths.get(image.uri),
                        Paths.get(newPathFile.path),
                        StandardCopyOption.REPLACE_EXISTING
                    )

                    // Delete old Image
                    if (newPathFile.exists()){
                        File(image.uri).delete()
                    }
                }
            }
        }
    }
}
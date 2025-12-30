package com.jonathanev.review.Domain

import com.jonathanev.review.data.Model.prueba.QuestionContent
import com.jonathanev.review.data.Model.prueba.QuestionItem
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.data.xml.Versions
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
        val oldPathImages = File(currentPath.path.replace(StorageFolders.GUIAS, StorageFolders.IMAGENES).replace(".xml", ""))

        if (version == Versions.VERSION2){
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
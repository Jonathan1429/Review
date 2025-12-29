package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants.VERSION1
import com.jonathanev.review.Core.Constants.VERSION2
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject

class MoverImagenesUseCase @Inject constructor(
    private val getRutaImagenesXMLUseCase: GetRutaImagenesXMLUseCase,
    private val getImagenesXMLUseCase: GetImagenesXMLUseCase,
    private val getImagenesEnDispositivoUseCase: GetImagenesEnDispositivoUseCase
) {
    operator fun invoke(
        version: String,
        oldPathFile: File,
        newPathFile: File,
        preguntas: MutableList<QuestionItem>,
        respuestas: MutableList<QuestionItem>
    ) {
        val oldPath = getRutaImagenesXMLUseCase.invoke(oldPathFile, version)
        val newPath = getRutaImagenesXMLUseCase.invoke(newPathFile, version)

        if (!newPath.exists()){
            newPath.mkdir()
        }

        val listImagesXML = getImagenesXMLUseCase.invoke(preguntas, respuestas)
        val currentDeviceNames = getImagenesEnDispositivoUseCase.invoke(oldPath)

        listImagesXML.filter { it.nameFile in currentDeviceNames }.forEach { image ->
            val initial = File(oldPath, image.nameFile)
            val destination = File(newPath, image.nameFile)

            if (initial.exists() && initial.isFile) {
                Files.move(
                    Paths.get(initial.path),
                    Paths.get(destination.path),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        }

        if (version == VERSION2){
            oldPath.deleteRecursively()
        }
    }
}
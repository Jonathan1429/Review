package com.jonathanev.review.Data

import com.jonathanev.review.Data.Model.GuideModel
import com.jonathanev.review.Data.Model.prueba.FolderModel
import com.jonathanev.review.Data.Model.prueba.QAItem
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import java.io.File

interface GuiaRepository {
    fun getXMLVersion(ruta: String): List<QAItem>
    fun getAttributesGuide(file: File): GuideModel
    fun getGuides(file: File): List<GuideModel>
    fun getFolders(file: File):List<FolderModel>
    fun getVersion(file: File): String
    fun setAttributesGuide(
        file: File,
        fileName: String,
        description: String,
        preguntas: List<QuestionItem>,
        respuestas: List<QuestionItem>
    ): Boolean
}
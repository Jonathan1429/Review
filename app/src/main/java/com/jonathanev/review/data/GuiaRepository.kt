package com.jonathanev.review.data

import com.jonathanev.review.data.Model.GuideModel
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.presentation.state.QAItem
import com.jonathanev.review.presentation.model.QuestionItem
import java.io.File

interface GuiaRepository {
    fun getXMLVersion(ruta: String): List<QAItem>
    fun getAttributesGuide(file: File): GuideModel
    fun getGuides(file: File): List<GuideModel>
    fun getFolders(file: File):List<FolderUiModel>
    fun getVersion(file: File): String
    fun setAttributesGuide(
        file: File,
        fileName: String,
        description: String,
        preguntas: List<QuestionItem>,
        respuestas: List<QuestionItem>
    ): Boolean
    fun saveFileV2(
        nameGuide: String,
        description: String,
        currentPath: File,
        preguntas: List<QuestionItem>,
        respuestas: List<QuestionItem>,
    ): Boolean
}
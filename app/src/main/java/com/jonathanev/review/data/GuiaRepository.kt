package com.jonathanev.review.data

import com.jonathanev.review.data.model.GuideXmlModel
import com.jonathanev.review.data.model.QAItemXml
import com.jonathanev.review.domain.model.QuestionItemDomain
import java.io.File

interface GuiaRepository {
    fun getXMLVersion(): List<QAItemXml>
    fun getAttributesGuide(file: File): GuideXmlModel
    fun getGuides(file: File): List<GuideXmlModel>
    fun getVersion(file: File): String
    fun setAttributesGuide(
        file: File,
        fileName: String,
        description: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>
    ): Boolean

    fun saveFileV2(
        nameGuide: String,
        description: String,
        currentPath: File,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
    ): Boolean
}
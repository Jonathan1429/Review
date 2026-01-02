package com.jonathanev.review.data

import com.jonathanev.review.data.model.GuideXmlModel
import com.jonathanev.review.data.model.QAItemXml
import com.jonathanev.review.domain.model.QuestionItemDomain
import java.io.File

interface GuiaRepository {
    fun getXMLVersion(): List<QAItemXml>
    fun getAttributesGuide(): GuideXmlModel
    fun getGuides(): List<GuideXmlModel>
    fun getVersion(): String
    fun setAttributesGuide(
        fileName: String,
        description: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>
    ): Boolean

    fun saveFileV2(
        nameGuide: String,
        description: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
    ): Boolean
}
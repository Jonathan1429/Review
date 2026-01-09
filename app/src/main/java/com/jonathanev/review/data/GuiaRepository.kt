package com.jonathanev.review.data

import com.jonathanev.review.data.model.GuideXmlModel
import com.jonathanev.review.data.model.QAItemXml
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.presentation.event.UIStopEvent

interface GuiaRepository {
    val guidesRecovery: List<GuideXmlModel>
    fun getXML(guideDomainModel: GuideDomainModel?): List<QAItemXml>
    fun getNumGuides(): Int

    //fun getAttributesGuide(): GuideXmlModel
    fun getGuides(): List<GuideXmlModel>

    //fun getVersion(nameGuide: String): String
    fun renameGuide(
        fileName: String,
        description: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        attributesGuide: GuideDomainModel
    ): Boolean

    fun saveFileV2(
        nameGuide: String,
        description: String,
        version: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
    ): Boolean

    fun deleteGuide(
        guideDomainModel: GuideDomainModel,
        listImages: List<QuestionContentDomain.Image>
    ): UIStopEvent
}
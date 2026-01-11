package com.jonathanev.review.data

import com.jonathanev.review.data.model.GuideXmlModel
import com.jonathanev.review.data.model.QAItemXml
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.presentation.folders.model.FolderAction

interface GuiaRepository {
    val guidesRecovery: List<GuideXmlModel>
    fun getXMLGuide(guideDomainModel: GuideDomainModel?): List<QAItemXml>
    fun getNumGuides(): Int
    fun getGuides(): List<GuideXmlModel>
    fun renameGuide(
        fileName: String,
        description: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        attributesGuide: GuideDomainModel
    ): Boolean
    fun moveGuide(mode: FolderAction.MovingFile): Boolean
    fun saveGuide(
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
    fun moveGuides()
}
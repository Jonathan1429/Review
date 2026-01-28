package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.result.GetGuideResult

interface GuiaRepository {
    val guidesRecovery: List<GuideDomainModel>
    fun getGuides(currentPathGuides: String): List<GuideDomainModel>
    fun getNumGuides(currentPathGuides: String): Int
    fun getXMLGuide(context: GuideContext): GetGuideResult
    fun saveGuide(
        guideDomainModel: GuideDomainModel,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>
    ): Boolean

    fun renameGuide(
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        guideContext: GuideContext.Rename,
    ): Boolean

    fun deleteGuide(
        deleteGuide: GuideContext.DeleteGuide,
    ): Boolean

    fun moveGuide(guideContext: GuideContext.Moving): Boolean
}
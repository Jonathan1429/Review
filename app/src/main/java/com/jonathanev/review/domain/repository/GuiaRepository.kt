package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.result.GetGuideResult

interface GuiaRepository {
    val guidesRecovery: List<GuideDomainModel>
    fun getGuides(currentPathGuides: String): List<GuideDomainModel>
    fun getNumGuides(currentPathGuides: String): Int
    fun getXMLGuide(guideContext: GuideContext.Actual): GetGuideResult
    fun saveGuide(
        nameGuide: String,
        description: String,
        version: GuideVersion,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        currentPathGuides: String,
    ): Boolean

    fun renameGuide(
        fileName: String,
        description: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        attributesGuide: GuideDomainModel,
        currentPathGuides: String
    ): Boolean

    fun deleteGuide(
        guideDomainModel: GuideDomainModel,
        currentPathGuides: String
    ): Boolean

    fun moveGuide(guideContext: GuideContext.Moving): Boolean
}
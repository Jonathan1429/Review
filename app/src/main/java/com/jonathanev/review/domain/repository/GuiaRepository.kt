package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.result.ExistGuideV1Result
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.GuideResource
import com.jonathanev.review.domain.result.SaveGuideErrors
import com.jonathanev.review.domain.result.UpdateGuideError

interface GuiaRepository {
    val guidesRecovery: List<GuideDomainModel>
    fun getGuides(relativeGuidePath: RelativeGuidePath): List<GuideDomainModel>
    fun getNumGuides(relativeGuidePath: RelativeGuidePath): Int
    fun getXMLGuide(
        guideDomainModel: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath
    ): GetGuideResult

    fun existXMLGuideV1(
        guideDomainModel: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath
    ): ExistGuideV1Result

    fun saveGuide(
        guideDomainModel: GuideDomainModel,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        relativeGuidePath: RelativeGuidePath
    ): GuideResource<GuideDomainModel, SaveGuideErrors>

    fun renameGuide(
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        guideContext: GuideContext.Rename,
    ): GuideResource<GuideDomainModel, UpdateGuideError>

    fun deleteGuide(
        deleteGuide: GuideContext.DeleteGuide,
    ): Boolean

    fun moveGuide(guideContext: GuideContext.Moving): Boolean
}
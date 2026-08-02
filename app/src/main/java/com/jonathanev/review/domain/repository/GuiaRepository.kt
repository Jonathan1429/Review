package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.result.ExistGuideV1Result
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.GuideResource
import com.jonathanev.review.domain.result.ReadGuideError
import com.jonathanev.review.domain.result.SaveGuideErrors
import com.jonathanev.review.domain.result.UpdateGuideError
import kotlinx.coroutines.flow.Flow

interface GuiaRepository {
    val guidesRecovery: List<GuideDomainModel>
    fun getGuides(): Flow<List<GuideDomainModel>>
    fun hasGuides(): Flow<Boolean>
    suspend fun getXMLGuide(guideDomainModel: GuideDomainModel): GetGuideResult

    suspend fun existXMLGuideV1(
        guideDomainModel: GuideDomainModel
    ): ExistGuideV1Result

    suspend fun saveGuide(
        guideDomainModel: GuideDomainModel,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>
    ): GuideResource<GuideDomainModel, SaveGuideErrors>

    suspend fun renameGuide(
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        guideContext: GuideContext.Rename,
    ): GuideResource<GuideDomainModel, UpdateGuideError>

    suspend fun deleteGuide(
        deleteGuide: GuideContext.DeleteGuide,
    ): Boolean

    suspend fun moveGuide(guideContext: GuideContext.Moving): Boolean

    suspend fun getVersionGuide(
        nameFile: String,
    ): GuideResource<GuideDomainModel, ReadGuideError>

    suspend fun existGuide(nameFile: String): Boolean
}
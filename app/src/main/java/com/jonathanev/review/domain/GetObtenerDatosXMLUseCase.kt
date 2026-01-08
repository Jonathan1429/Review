package com.jonathanev.review.domain

import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.data.mapper.toIconType
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QAItemDomain
import javax.inject.Inject

class GetObtenerDatosXMLUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
){
    operator fun invoke(guideDomainModel: GuideDomainModel?): List<QAItemDomain> {
        val qaItemXml = guiaRepository.getXML(guideDomainModel)
        val qaItemDomain = qaItemXml.map { it.toIconType() }
        return qaItemDomain
    }
}
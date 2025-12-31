package com.jonathanev.review.Domain

import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.presentation.state.QAItemDomain
import javax.inject.Inject

class GetObtenerDatosXMLUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
){
    operator fun invoke(ruta: String): List<QAItemDomain> {
        return guiaRepository.getXMLVersion(ruta)
    }
}
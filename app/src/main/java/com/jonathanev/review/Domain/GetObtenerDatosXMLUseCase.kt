package com.jonathanev.review.Domain

import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.GuiaRepositoryImpl
import com.jonathanev.review.Data.Model.prueba.QAItem
import javax.inject.Inject

class GetObtenerDatosXMLUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
){
    operator fun invoke(ruta: String): List<QAItem> {
        return guiaRepository.getXMLVersion(ruta)
    }
}
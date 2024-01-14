package com.jonathanev.review.Data

import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.GuiaProvider
import com.jonathanev.review.Domain.getAllGuiasUseCase
import javax.inject.Inject

class GuiaRepository @Inject constructor(
    val getAllGuiasUseCase: getAllGuiasUseCase,
    val guiaProvider: GuiaProvider
){
    fun getGuias():List<GuiaModel>{
        guiaProvider.guias = getAllGuiasUseCase()!!
        return guiaProvider.guias
    }
}
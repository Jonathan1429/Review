package com.jonathanev.review.Data

import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.GuiaProvider
import com.jonathanev.review.Domain.getAllGuiasCarpetaSeleccionadaUseCase
import com.jonathanev.review.Domain.getAllGuiasUseCase
import javax.inject.Inject

class GuiaRepository @Inject constructor(
    val getAllGuiasUseCase: getAllGuiasUseCase,
    val getAllGuiasCarpetaSeleccionadaUseCase: getAllGuiasCarpetaSeleccionadaUseCase,
    val guiaProvider: GuiaProvider
){
    fun getGuias():List<GuiaModel>{
        guiaProvider.guias = getAllGuiasUseCase()!!
        return guiaProvider.guias
    }

    fun getGuiasCarpetaSeleccionada(carpetaSeleccionada: String):List<GuiaModel>{
        guiaProvider.guias = getAllGuiasCarpetaSeleccionadaUseCase(carpetaSeleccionada)!!
        return guiaProvider.guias
    }
}
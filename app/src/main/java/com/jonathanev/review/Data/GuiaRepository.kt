package com.jonathanev.review.Data

import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.GuiaProvider
import com.jonathanev.review.Domain.getAllGuiasCarpetaSeleccionadaUseCase
import com.jonathanev.review.Domain.getAllGuiasUseCase
import java.io.File
import javax.inject.Inject

class GuiaRepository @Inject constructor(
    val getAllGuiasUseCase: getAllGuiasUseCase,
    val guiaProvider: GuiaProvider
){
    fun getGuias(file: File):List<GuiaModel>{
        guiaProvider.guias = getAllGuiasUseCase(file)!!
        return guiaProvider.guias
    }
}
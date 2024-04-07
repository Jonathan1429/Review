package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.GuiaProvider
import javax.inject.Inject

class getGuiaPosicionUseCase @Inject constructor(
    private val guiaProvider: GuiaProvider
){
    operator fun invoke(posicion: Int): GuiaModel {
        return guiaProvider.guias[posicion]
    }
}
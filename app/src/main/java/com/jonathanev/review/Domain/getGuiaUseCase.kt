package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.GuiaProvider
import javax.inject.Inject

class getGuiaUseCase @Inject constructor(
    val guiaProvider: GuiaProvider
){
    operator fun invoke(position: Int): GuiaModel {
        return guiaProvider.guias[position]
    }
}
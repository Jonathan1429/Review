package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.GuiaProvider
import javax.inject.Inject

class getGuiaUseCase @Inject constructor(
    private val guiaProvider: GuiaProvider
) {
    operator fun invoke(ruta: String): GuiaModel {
        val fileSelected = ruta.substringAfterLast("/")

        return guiaProvider.guias
            .firstOrNull { "${it.nombreGuia}.xml" == fileSelected }
            ?.let { GuiaModel(it.nombreGuia, 0) } ?: GuiaModel("", 0)
    }
}
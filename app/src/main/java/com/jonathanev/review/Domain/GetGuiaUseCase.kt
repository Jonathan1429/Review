package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.provider.GuiaProvider
import javax.inject.Inject

class GetGuiaUseCase @Inject constructor(
    private val guiaProvider: GuiaProvider
) {
    operator fun invoke(ruta: String): GuiaModel {
        val fileSelected = ruta.substringAfterLast("/")

        return guiaProvider.guias
            .firstOrNull { "${it.nombreGuia}.xml" == fileSelected }
            ?.let { GuiaModel(
                it.nombreGuia,
                imgGuia = 0,
            ) } ?: GuiaModel("", imgGuia = 0)
    }
}
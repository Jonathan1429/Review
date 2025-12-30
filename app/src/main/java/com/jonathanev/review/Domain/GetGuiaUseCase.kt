package com.jonathanev.review.Domain

import com.jonathanev.review.data.Model.GuideModel
import com.jonathanev.review.data.provider.GuiaProvider
import javax.inject.Inject

class GetGuiaUseCase @Inject constructor(
    private val guiaProvider: GuiaProvider
) {
    operator fun invoke(ruta: String): GuideModel {
        return GuideModel("", "")
        /*val fileSelected = ruta.substringAfterLast("/")

        return guiaProvider.guias
            .firstOrNull { "${it.nombreGuia}.xml" == fileSelected }
            ?.let { GuideModel(
                it.nombreGuia,
            ) } ?: GuideModel("")*/
    }
}
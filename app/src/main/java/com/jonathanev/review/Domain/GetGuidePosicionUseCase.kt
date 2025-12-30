package com.jonathanev.review.Domain

import com.jonathanev.review.data.Model.GuideModel
import com.jonathanev.review.data.Model.GuideResult
import javax.inject.Inject

class GetGuidePosicionUseCase @Inject constructor() {
    operator fun invoke(position: Int, guides: List<GuideModel>): GuideResult {
        return guides.getOrNull(position)?.let { GuideResult.Success(it) }
            ?: GuideResult.Error("No se encontró la carpeta en la posición $position")
    }
}
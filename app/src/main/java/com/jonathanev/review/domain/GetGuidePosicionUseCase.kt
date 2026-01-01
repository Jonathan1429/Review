package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideResultDomain
import javax.inject.Inject

class GetGuidePosicionUseCase @Inject constructor() {
    operator fun invoke(position: Int, guides: List<GuideDomainModel>): GuideResultDomain {
        return guides.getOrNull(position)?.let { GuideResultDomain.Success(it) }
            ?: GuideResultDomain.Error("No se encontró la carpeta en la posición $position")
    }
}
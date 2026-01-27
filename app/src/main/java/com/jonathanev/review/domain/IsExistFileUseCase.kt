package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import javax.inject.Inject

class IsExistFileUseCase @Inject constructor() {
    operator fun invoke(
        cachedGuides: List<GuideDomainModel>,
        name: String
    ): Boolean {
        val guideDomainModel = cachedGuides.find { it.nameGuide == name }
        return guideDomainModel != null
    }
}
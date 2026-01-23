package com.jonathanev.review.domain.result

import com.jonathanev.review.domain.model.GuideDomainModel

sealed class GuideResultDomain {
    data class Success(val guideDomainModel: GuideDomainModel) : GuideResultDomain()
    data object Error : GuideResultDomain()
}
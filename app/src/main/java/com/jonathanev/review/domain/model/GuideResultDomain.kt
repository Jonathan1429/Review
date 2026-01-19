package com.jonathanev.review.domain.model

sealed class GuideResultDomain {
    data class Success(val guideDomainModel: GuideDomainModel) : GuideResultDomain()
    data object Error : GuideResultDomain()
}
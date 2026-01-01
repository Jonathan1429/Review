package com.jonathanev.review.domain.model

sealed class GuideResultDomain {
    data class Success(val guideDomainModel: GuideDomainModel) : GuideResultDomain()
    data class Error(val message: String) : GuideResultDomain()
}
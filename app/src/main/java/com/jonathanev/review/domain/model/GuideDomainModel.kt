package com.jonathanev.review.domain.model

data class GuideDomainModel(
    val version: GuideVersion,
    val nameGuide: String,
    val description: String
)
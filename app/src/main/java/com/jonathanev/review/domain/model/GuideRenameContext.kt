package com.jonathanev.review.domain.model

data class GuideRenameContext(
    val oldGuide: GuideDomainModel,
    val newName: String
)
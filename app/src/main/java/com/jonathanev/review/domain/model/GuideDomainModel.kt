package com.jonathanev.review.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GuideDomainModel(
    val version: GuideVersion,
    val nameGuide: String,
    val description: String
): Parcelable
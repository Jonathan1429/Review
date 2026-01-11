package com.jonathanev.review.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GuideDomainModel(
    val version: String,
    val nameGuide: String,
    val description: String
): Parcelable
package com.jonathanev.review.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class RelativeGuidePath(val value: String) : Parcelable

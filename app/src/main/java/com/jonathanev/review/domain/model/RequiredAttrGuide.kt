package com.jonathanev.review.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class RequiredAttrGuide(val value: String) : Parcelable {
    init {
        require(value.isNotBlank()) { "AttrGuide no puede estar vacío" }
    }
}
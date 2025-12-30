package com.jonathanev.review.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class ActionGuide: Parcelable {
    @Parcelize
    data object NONE: ActionGuide()
    @Parcelize
    data object CREATE: ActionGuide()
    @Parcelize
    data class EDIT(val posGuide: Int): ActionGuide()
}
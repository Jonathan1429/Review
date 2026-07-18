package com.jonathanev.review.presentation.model

import kotlinx.serialization.Serializable

@Serializable
sealed class ActionGuide {
    @Serializable
    data object NONE : ActionGuide()

    @Serializable
    data class CREATE(val nameGuide: String, val description: String) : ActionGuide()

    @Serializable
    data class EDIT(val nameGuide: String, val description: String, val noQuestion: Int) :
        ActionGuide()
}
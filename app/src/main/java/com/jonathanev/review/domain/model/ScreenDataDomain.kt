package com.jonathanev.review.domain.model

data class ScreenDataDomain(
    val name: String,
    val description: String = "",
    val imgFolder: String,
    val color: Int,
    val version: Int = 1
)
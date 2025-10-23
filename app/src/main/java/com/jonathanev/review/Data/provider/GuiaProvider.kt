package com.jonathanev.review.Data.provider

import com.jonathanev.review.Data.Model.GuiaModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuiaProvider @Inject constructor() {
    var guias: List<GuiaModel> = emptyList()
}
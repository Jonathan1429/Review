package com.jonathanev.review.Data.Model

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuiaProvider @Inject constructor() {
    var guias: List<GuiaModel> = emptyList()
}
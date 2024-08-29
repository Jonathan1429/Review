package com.jonathanev.review.Data.Model

// Clase de datos para encapsular el resultado
data class GuiasVerificacionModel(
    val message: String?,
    val contadorPregunta: Int,
    val shouldUpdateLabel: Boolean,
    val preguntas: ArrayList<String>,
    val respuestas: ArrayList<String>
)
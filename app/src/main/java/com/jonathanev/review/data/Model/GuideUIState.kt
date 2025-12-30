package com.jonathanev.review.data.Model

import android.net.Uri
import com.jonathanev.review.data.Model.prueba.QuestionItem
import com.jonathanev.review.data.Model.prueba.TypeContent

data class GuideUiState(
    val preguntas: List<QuestionItem> = emptyList(),
    val respuestas: List<QuestionItem> = emptyList(),
    val contadorPregunta: Int = 0,
    val contadorContenido: Int = -1,
    val typeContent: TypeContent = TypeContent.QUESTION,
    val fileName: String = "",
    val isEditing: Boolean = false,
    val actualUri: Uri? = null
)


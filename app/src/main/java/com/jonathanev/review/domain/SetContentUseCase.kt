package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import javax.inject.Inject

class SetContentUseCase @Inject constructor() {
    operator fun invoke(
        newContent: QuestionContentDomain,
        sourceList: List<QuestionItemDomain>,
        contadorPregunta: Int,
        contadorContenido: Int,
        isEditingMode: Boolean,
        filterType: Class<out QuestionContentDomain>
    ): List<QuestionItemDomain> {
        // Validar que el índice de la pregunta sea correcto
        if (contadorPregunta !in sourceList.indices) return sourceList

        val updatedItem = sourceList[contadorPregunta].let { originalItem ->
            val originalContent = originalItem.content.toMutableList()

            // 1. Mapear contenidos del tipo específico para encontrar el índice real
            val filteredWithIndices = originalItem.content
                .mapIndexed { index, content -> index to content }
                .filter { filterType.isInstance(it.second) }

            if (isEditingMode && contadorContenido in filteredWithIndices.indices) {
                // Modo edición: Reemplazamos en la posición original exacta
                val realIndexInOriginal = filteredWithIndices[contadorContenido].first
                originalContent[realIndexInOriginal] = newContent
            } else {
                // Modo creación: Añadimos al final
                originalContent.add(newContent)
            }

            originalItem.copy(content = originalContent)
        }

        return sourceList.toMutableList().apply {
            this[contadorPregunta] = updatedItem
        }
    }
}
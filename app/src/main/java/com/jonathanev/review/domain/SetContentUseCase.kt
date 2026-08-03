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

            // Mapeamos los elementos del tipo específico conservando su índice original
            val filteredWithIndices = originalItem.content
                .mapIndexed { index, content -> index to content }
                .filter { filterType.isInstance(it.second) }

            if (isEditingMode && contadorContenido in filteredWithIndices.indices) {
                // Reemplazar
                val realIndex = filteredWithIndices[contadorContenido].first
                originalContent[realIndex] = newContent
            } else {
                // INSERTAR Y DESPLAZAR
                val targetIndex = filteredWithIndices.getOrNull(contadorContenido)?.first

                if (targetIndex != null) {
                    // Si existe, inserta en 'targetIndex' y desplaza el resto hacia la derecha
                    originalContent.add(targetIndex, newContent)
                } else {
                    // Si 'contadorContenido' está fuera de rango (no hay nada que desplazar), se añade al final
                    originalContent.add(newContent)
                }
            }

            originalItem.copy(content = originalContent)
        }

        return sourceList.toMutableList().apply {
            this[contadorPregunta] = updatedItem
        }
    }
}
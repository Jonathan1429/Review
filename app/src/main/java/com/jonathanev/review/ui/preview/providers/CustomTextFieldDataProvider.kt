package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

data class PropertiesTF(
    val name: String,
    val label: String
){
    override fun toString(): String {
        return "Label_$label"
    }
}

class CustomTextFieldDataProvider: PreviewParameterProvider<PropertiesTF> {
    override val values: Sequence<PropertiesTF>
        get() = sequenceOf(
            PropertiesTF(
                name = "Kotlin",
                label = "Nombra tu carpeta"
            ),
            PropertiesTF(
                name = "Sintaxis",
                label = "Nombra tu archivo"
            ),
            PropertiesTF(
                name = "Guia de Sintaxis para Kotlin",
                label = "Descripción (Opcional)"
            )
        )

    override fun getDisplayName(index: Int): String? {
        return when(index){
            0 -> "Nombrar_carpeta"
            1 -> "Nombrar_archivo"
            2 -> "Descripcion"
            else -> super.getDisplayName(index)
        }
    }
}
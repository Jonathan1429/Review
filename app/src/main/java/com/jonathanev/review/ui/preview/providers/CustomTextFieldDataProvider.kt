package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

data class PropertiesTF(
    val name: String,
    val label: String
)

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
}
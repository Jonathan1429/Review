package com.jonathanev.review.presentation.state

import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.IconType

data class PropertiesFilesState(
    val name: String = "",
    val description: String = "",
    val oldName: String = "",
    val oldDescription: String = "",
    val icon: IconType = IconType.ANCHOR_SOLID_FULL,
    val color: ColorType = ColorType.Default,
    val selectedIndex: Int = -1,
    val icons: List<IconType> = emptyList(),
    val showOverwriteDialogFile: Boolean = false,
    val showOverwriteDialogFolder: Boolean = false
) {
    companion object {
        // 1. Define las listas de íconos como constantes de prueba independientes
        val FileIconsMock = listOf(
            IconType.LIGHTBULB
        )

        val FolderIconsMock = listOf(
            IconType.BACTERIA_SOLID_FULL,
            IconType.ANCHOR_SOLID_FULL,
            IconType.ANGELLIST_BRANDS_SOLID_FULL
        )
    }
}
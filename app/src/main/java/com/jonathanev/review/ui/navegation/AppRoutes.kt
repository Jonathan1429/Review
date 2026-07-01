package com.jonathanev.review.ui.navegation

import androidx.navigation3.runtime.NavKey
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.ui.model.PropertiesGuide
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoutes : NavKey {
    @Serializable
    data object ListFoldersScreen: AppRoutes
    @Serializable
    data object MainScreen : AppRoutes
    @Serializable
    data class CreateFilesPropertiesScreen(val folderAction: FolderAction): AppRoutes

    @Serializable
    data class FillingGuideScreen(val propertiesGuide: PropertiesGuide): AppRoutes

    /*@Serializable
    data class PrevisualizacionScreen(val uuid: String): AppRoutes*/
}
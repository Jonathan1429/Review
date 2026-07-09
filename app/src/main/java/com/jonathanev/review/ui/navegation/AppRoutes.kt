package com.jonathanev.review.ui.navegation

import androidx.navigation3.runtime.NavKey
import com.jonathanev.review.presentation.model.ActionGuide
import com.jonathanev.review.presentation.model.FolderAction
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoutes : NavKey {
    @Serializable
    data class ListGuidesScreen(val folderAction: FolderAction = FolderAction.None): AppRoutes
    @Serializable
    data class MainScreen(val folderAction: FolderAction = FolderAction.None) : AppRoutes
    @Serializable
    data class CreateFilesPropertiesScreen(val folderAction: FolderAction): AppRoutes

    @Serializable
    data class FillingGuideScreen(val actionGuide: ActionGuide): AppRoutes

    @Serializable
    data class PreviewQuestionsScreen(val nameGuide: String): AppRoutes
}
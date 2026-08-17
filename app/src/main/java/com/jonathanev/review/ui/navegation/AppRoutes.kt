package com.jonathanev.review.ui.navegation

import androidx.navigation3.runtime.NavKey
import com.jonathanev.review.presentation.model.FileFormMode
import com.jonathanev.review.presentation.model.QuestionContentMode
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoutes : NavKey {
    @Serializable
    data object WithoutFoldersScreen : AppRoutes

    @Serializable
    data object WithoutGuidesScreen : AppRoutes

    @Serializable
    data object ListFoldersScreen : AppRoutes

    @Serializable
    data object ListGuidesScreen : AppRoutes
    @Serializable
    data object EntryGuidesScreen : AppRoutes

    @Serializable
    data object MainScreen : AppRoutes
    @Serializable
    data class CreateFilesPropertiesScreen(val fileFormMode: FileFormMode): AppRoutes

    @Serializable
    data object FillingGuideScreen : AppRoutes

    @Serializable
    data class CreateImageScreen(
        val questionContentMode: QuestionContentMode,
        val posItem: Int
    ) : AppRoutes

    @Serializable
    data class CreateTextScreen(
        val questionContentMode: QuestionContentMode,
        val posItem: Int
    ) : AppRoutes
    @Serializable
    data object PreviewQuestionsScreen : AppRoutes
}
package com.jonathanev.review.ui.navegation

import androidx.navigation3.runtime.NavKey
import com.jonathanev.review.presentation.model.ActionGuide
import com.jonathanev.review.presentation.model.FileFormMode
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.presentation.model.GuideMode
import com.jonathanev.review.presentation.model.QuestionContentUi
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoutes : NavKey {
    @Serializable
    data class ListGuidesScreen(val fileInteractionMode: FileInteractionMode = FileInteractionMode.Default): AppRoutes
    @Serializable
    data class MainScreen(val fileInteractionMode: FileInteractionMode = FileInteractionMode.Default) : AppRoutes
    @Serializable
    data class CreateFilesPropertiesScreen(val fileFormMode: FileFormMode): AppRoutes

    @Serializable
    data class FillingGuideScreen(val guideMode: GuideMode): AppRoutes

    data class CreateImageScreen(val contentType: QuestionContentUi.Image): AppRoutes

    data class CreateTextScreen(val contentType: QuestionContentUi.Text): AppRoutes
    @Serializable
    data class PreviewQuestionsScreen(val nameGuide: String): AppRoutes
}
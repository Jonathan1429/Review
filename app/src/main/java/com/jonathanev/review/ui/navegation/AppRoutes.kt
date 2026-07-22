package com.jonathanev.review.ui.navegation

import androidx.navigation3.runtime.NavKey
import com.jonathanev.review.presentation.model.FileFormMode
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.model.GuideMode
import com.jonathanev.review.presentation.model.QuestionContentMode
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

    @Serializable
    data class CreateImageScreen(
        val questionContentMode: QuestionContentMode,
        val contentType: QuestionContentUi.Image,
        val guideMode: GuideMode
    ) : AppRoutes

    data class CreateTextScreen(
        val questionContentMode: QuestionContentMode,
        val contentType: QuestionContentUi.Text,
        val guideMode: GuideMode
    ) : AppRoutes
    @Serializable
    data class PreviewQuestionsScreen(val nameGuide: String): AppRoutes
}
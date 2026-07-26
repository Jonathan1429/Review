package com.jonathanev.review.ui.navegation

import androidx.navigation3.runtime.NavKey
import com.jonathanev.review.presentation.model.FileFormMode
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.presentation.model.GuideMode
import com.jonathanev.review.presentation.model.QuestionContentMode
import com.jonathanev.review.presentation.model.QuestionContentUi
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoutes : NavKey {
    @Serializable
    data object WithoutFoldersScreen : AppRoutes

    @Serializable
    data class WithoutGuidesScreen(val fileInteractionMode: FileInteractionMode = FileInteractionMode.Default) :
        AppRoutes

    @Serializable
    data class ListFoldersScreen(
        val fileInteractionMode: FileInteractionMode = FileInteractionMode.Default,
        val listFolders: List<FolderUiModel>
    ) : AppRoutes

    @Serializable
    data class ListGuidesScreen(val fileInteractionMode: FileInteractionMode = FileInteractionMode.Default) :
        AppRoutes
    @Serializable
    data class EntryGuidesScreen(val fileInteractionMode: FileInteractionMode = FileInteractionMode.Default) :
        AppRoutes

    @Serializable
    data object MainScreen : AppRoutes
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
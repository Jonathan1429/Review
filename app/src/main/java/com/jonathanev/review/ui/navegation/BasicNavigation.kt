package com.jonathanev.review.ui.navegation

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jonathanev.review.presentation.model.FileFormMode
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.model.GuideMode
import com.jonathanev.review.presentation.model.QuestionContentMode
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.viewmodel.CreateFilesViewModel
import com.jonathanev.review.presentation.viewmodel.FragReviewEntryViewModel
import com.jonathanev.review.presentation.viewmodel.FragmentListGuidesViewModel
import com.jonathanev.review.presentation.viewmodel.FragmentWithoutFilesViewModel
import com.jonathanev.review.presentation.viewmodel.ListFoldersViewModel
import com.jonathanev.review.presentation.viewmodel.MainActivityViewModel
import com.jonathanev.review.presentation.viewmodel.PreviewViewModel
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.ui.model.ContentType
import com.jonathanev.review.ui.screens.CreateFilesPropertiesRoute
import com.jonathanev.review.ui.screens.CreateImageRoute
import com.jonathanev.review.ui.screens.CreateTextRoute
import com.jonathanev.review.ui.screens.FillingGuideRoute
import com.jonathanev.review.ui.screens.GuidesEntryRoute
import com.jonathanev.review.ui.screens.ListFoldersRoute
import com.jonathanev.review.ui.screens.ListGuidesRoute
import com.jonathanev.review.ui.screens.MainActivityEntryRoute
import com.jonathanev.review.ui.screens.PreviewQuestionsRoute
import com.jonathanev.review.ui.screens.WithoutFilesRoute
import com.jonathanev.review.ui.screens.WithoutFoldersScreen

@Composable
fun BasicNavigation() {
    val backStack = rememberNavBackStack(AppRoutes.MainScreen)

    NavDisplay(
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        transitionSpec = {
            slideInHorizontally(
                animationSpec = tween(1000),
                initialOffsetX = { it }
            ) togetherWith slideOutHorizontally(
                animationSpec = tween(1000),
                targetOffsetX = { -it }
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                animationSpec = tween(1000),
                initialOffsetX = { -it }
            ) togetherWith slideOutHorizontally(
                animationSpec = tween(1000),
                targetOffsetX = { it }
            )
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                animationSpec = tween(1000),
                initialOffsetX = { -it }
            ) togetherWith slideOutHorizontally(
                animationSpec = tween(1000),
                targetOffsetX = { it }
            )
        },
        entryProvider = entryProvider {
            val viewModelSharedCreateFile: SharedFragmentCreateFileViewModel = viewModel()

            entry<AppRoutes.MainScreen> {
                val viewModel: MainActivityViewModel = viewModel()

                MainActivityEntryRoute(
                    viewModel = viewModel,
                    onNavWithoutFolderScreen = {
                        backStack.add(AppRoutes.WithoutFoldersScreen)
                    },
                    onNavListFoldersScreen = { listFolders ->
                        backStack.add(AppRoutes.ListFoldersScreen(listFolders = listFolders))
                    }
                )
            }
            entry<AppRoutes.WithoutFoldersScreen> {
                WithoutFoldersScreen(
                    onNavCreateFilesProperties = {
                        backStack.add(AppRoutes.CreateFilesPropertiesScreen(FileFormMode.CreatingFolder))
                    }
                )
            }
            entry<AppRoutes.ListFoldersScreen> { values ->
                val viewModel: ListFoldersViewModel = viewModel()

                ListFoldersRoute(
                    viewModel = viewModel,
                    guias = values.listFolders,
                    onCreateFolderClick = {
                        backStack.add(AppRoutes.CreateFilesPropertiesScreen(FileFormMode.CreatingFolder))
                    },
                    onFolderClick = { folderAction ->
                        backStack.add(AppRoutes.EntryGuidesScreen(folderAction))
                    },
                    fileInteractionMode = values.fileInteractionMode
                )
            }
            entry<AppRoutes.EntryGuidesScreen> { action ->
                val viewModel: FragReviewEntryViewModel = viewModel()

                GuidesEntryRoute(
                    viewModel = viewModel,
                    onNavigateListGuidesRoute = {
                        backStack.add(
                            AppRoutes.ListGuidesScreen(action.fileInteractionMode)
                        )
                    },
                    onNavigateWithoutFilesScreen = {
                        backStack.add(AppRoutes.WithoutGuidesScreen(action.fileInteractionMode))
                    },
                )
            }
            entry<AppRoutes.WithoutGuidesScreen> { value ->
                val viewModel: FragmentWithoutFilesViewModel = viewModel()

                WithoutFilesRoute(
                    viewModel = viewModel,
                    fileInteractionMode = value.fileInteractionMode,
                    onAddGuideClick = {
                        backStack.add(AppRoutes.CreateFilesPropertiesScreen(FileFormMode.CreatingFile))
                    }
                )
            }
            entry<AppRoutes.ListGuidesScreen> { value ->
                val viewModel: FragmentListGuidesViewModel = viewModel()

                ListGuidesRoute(
                    viewModel = viewModel,
                    fileInteractionMode = value.fileInteractionMode,
                    onAddGuideClick = {
                        backStack.add(AppRoutes.CreateFilesPropertiesScreen(FileFormMode.CreatingFile))
                    },
                    onOpenGuideClick = {
                        backStack.add(AppRoutes.PreviewQuestionsScreen)
                    },
                    onDeleteGuideClick = {
                        backStack.removeLastOrNull()
                    },
                    onRenameGuideClick = { guideUIModel ->
                        backStack.add(
                            AppRoutes.CreateFilesPropertiesScreen(
                                FileFormMode.RenameFile(
                                    guideUIModel
                                )
                            )
                        )
                    },
                    onMoveGuideClick = {
                        backStack.clear()
                        backStack.add(AppRoutes.ListGuidesScreen(FileInteractionMode.MovingItem))
                    },
                    onBackNav = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateWithoutFilesScreen = {
                        backStack.add(AppRoutes.WithoutGuidesScreen())
                    }
                )
            }
            entry<AppRoutes.CreateFilesPropertiesScreen> { mode ->
                val viewModel: CreateFilesViewModel = viewModel()

                CreateFilesPropertiesRoute(
                    viewModel = viewModel,
                    fileFormMode = mode.fileFormMode,
                    onNavBack = { backStack.removeLastOrNull() },
                    onNavFillingGuide = { propertiesGuide ->
                        backStack.add(
                            AppRoutes.FillingGuideScreen(
                                GuideMode.Create(propertiesGuide.name, propertiesGuide.description)
                            )
                        )
                    }
                )
            }
            entry<AppRoutes.FillingGuideScreen> { action ->
                val context = LocalContext.current

                FillingGuideRoute(
                    viewModel = viewModelSharedCreateFile,
                    guideMode = action.guideMode,
                    onOpenAssetClick = { typeContent ->
                        when (typeContent) {
                            is QuestionContentUi.Image -> {
                                backStack.add(
                                    AppRoutes.CreateImageScreen(
                                        questionContentMode = QuestionContentMode.EDITING,
                                        contentType = QuestionContentUi.Image(
                                            uri = typeContent.uri,
                                            nameFile = typeContent.nameFile
                                        ),
                                        guideMode = action.guideMode
                                    )
                                )
                            }

                            QuestionContentUi.None -> {
                                Toast.makeText(
                                    context,
                                    "No se puede procesar la solicitud",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            is QuestionContentUi.Text -> {
                                backStack.add(
                                    AppRoutes.CreateTextScreen(
                                        questionContentMode = QuestionContentMode.EDITING,
                                        contentType = QuestionContentUi.Text(
                                            typeContent.text,
                                            typeContent.colorRanges
                                        ),
                                        guideMode = action.guideMode
                                    )
                                )
                            }
                        }
                    },
                    onAddAssetClick = { mediaSelected ->
                        when (mediaSelected) {
                            ContentType.TEXT ->
                                backStack.add(
                                    AppRoutes.CreateTextScreen(
                                        questionContentMode = QuestionContentMode.CREATING,
                                        contentType = QuestionContentUi.Text(
                                            text = "",
                                            colorRanges = emptyList()
                                        ),
                                        guideMode = action.guideMode
                                    )
                                )

                            ContentType.IMAGE -> {
                                backStack.add(
                                    AppRoutes.CreateImageScreen(
                                        questionContentMode = QuestionContentMode.CREATING,
                                        contentType = QuestionContentUi.Image(
                                            uri = "",
                                            nameFile = ""
                                        ),
                                        guideMode = action.guideMode
                                    )
                                )
                            }
                        }
                    },
                    onActionGuideNone = {
                        Toast.makeText(
                            context,
                            "No se puede procesar la solicitud",
                            Toast.LENGTH_SHORT
                        ).show()
                        backStack.removeLastOrNull()
                    },
                    onCloseGuide = {
                        backStack.clear()
                        backStack.add(AppRoutes.MainScreen)
                    }
                )
            }
            entry<AppRoutes.CreateImageScreen> { values ->
                CreateImageRoute(
                    questionContentMode = values.questionContentMode,
                    guideMode = values.guideMode,
                    contentType = values.contentType,
                    viewModel = viewModelSharedCreateFile,
                    imageUploaded = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<AppRoutes.CreateTextScreen> { values ->
                CreateTextRoute(
                    questionContentMode = values.questionContentMode,
                    guideMode = values.guideMode,
                    viewModel = viewModelSharedCreateFile,
                    contentType = values.contentType,
                    onSaveText = { backStack.removeLastOrNull() },
                    onBackNav = { backStack.removeLastOrNull() }
                )
            }
            entry<AppRoutes.PreviewQuestionsScreen> {
                val viewModel: PreviewViewModel = viewModel()

                val propertiesGuide = viewModel.uiState.collectAsStateWithLifecycle().value

                PreviewQuestionsRoute(
                    viewModel = viewModel,
                    onEditingGuideClick = { nameGuide: String, descriptionGuide: String, posQuestionEdit: Int ->
                        backStack.add(
                            AppRoutes.FillingGuideScreen(
                                GuideMode.Edit(
                                    nameGuide,
                                    descriptionGuide,
                                    posQuestionEdit
                                )
                            )
                        )
                    },
                    onPlayGuideClick = { nameGuide: String, posQuestionPlay: Int ->
                        backStack.add(
                            AppRoutes.FillingGuideScreen(
                                GuideMode.Review(
                                    nameGuide = nameGuide,
                                    posQuestion = posQuestionPlay
                                )
                            )
                        )
                    }
                )
            }
        }
    )
}
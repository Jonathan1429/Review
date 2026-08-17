package com.jonathanev.review.ui.navegation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jonathanev.review.presentation.model.FileFormMode
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
import com.jonathanev.review.presentation.viewmodel.WithoutFoldersViewModel
import com.jonathanev.review.ui.model.ContentType
import com.jonathanev.review.ui.screens.CreateFilesPropertiesRoute
import com.jonathanev.review.ui.screens.CreateImageRoute
import com.jonathanev.review.ui.screens.CreateTextRoute
import com.jonathanev.review.ui.screens.StudyGuideRoute
import com.jonathanev.review.ui.screens.GuidesEntryRoute
import com.jonathanev.review.ui.screens.ListFoldersRoute
import com.jonathanev.review.ui.screens.ListGuidesRoute
import com.jonathanev.review.ui.screens.MainActivityEntryRoute
import com.jonathanev.review.ui.screens.PreviewQuestionsRoute
import com.jonathanev.review.ui.screens.WithoutFilesRoute
import com.jonathanev.review.ui.screens.WithoutFoldersRoute
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun BasicNavigation() {
    val backStack = rememberNavBackStack(AppRoutes.MainScreen)
    val context = LocalContext.current

    // Estado y efecto para el aviso de doble toque para salir
    var showExitWarning by remember { mutableStateOf(false) }

    BackHandler(enabled = backStack.size == 1) {
        if (showExitWarning) {
            (context as? Activity)?.finish()
        } else {
            Toast.makeText(context, "Presiona atrás de nuevo para salir", Toast.LENGTH_SHORT).show()
            showExitWarning = true
        }
    }

    LaunchedEffect(showExitWarning) {
        if (showExitWarning) {
            delay(2000L.milliseconds)
            showExitWarning = false
        }
    }

    // ViewModel compartido para el flujo de creación/edición de guía
    val viewModelSharedCreateFile: SharedFragmentCreateFileViewModel = hiltViewModel()

    // Detectamos si el usuario se encuentra dentro de alguna pantalla del flujo compartido
    val isSharedFlowActive = backStack.any { route ->
        route is AppRoutes.StudyGuideScreen ||
                route is AppRoutes.CreateImageScreen ||
                route is AppRoutes.CreateTextScreen
    }

    // Al salir completamente del flujo, limpiamos el estado retenido en el ViewModel
    LaunchedEffect(isSharedFlowActive) {
        if (!isSharedFlowActive) {
            viewModelSharedCreateFile.onDiscardGuide()
        }
    }

    NavDisplay(
        backStack = backStack,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
            entry<AppRoutes.MainScreen> {
                val viewModel: MainActivityViewModel = viewModel()

                MainActivityEntryRoute(
                    viewModel = viewModel,
                    onNavWithoutFolderScreen = {
                        if (backStack.isNotEmpty()) {
                            backStack[backStack.lastIndex] = AppRoutes.WithoutFoldersScreen
                        }
                    },
                    onNavListFoldersScreen = {
                        if (backStack.isNotEmpty()) {
                            backStack[backStack.lastIndex] = AppRoutes.ListFoldersScreen
                        }
                    }
                )
            }

            entry<AppRoutes.WithoutFoldersScreen> {
                val viewModel: WithoutFoldersViewModel = viewModel()

                WithoutFoldersRoute(
                    viewModel = viewModel,
                    onNavCreateFilesProperties = {
                        backStack.add(AppRoutes.CreateFilesPropertiesScreen(FileFormMode.CreatingFolder))
                    },
                    onNavListFolders = {
                        if (backStack.isNotEmpty()) {
                            backStack[backStack.lastIndex] = AppRoutes.ListFoldersScreen
                        }
                    }
                )
            }

            entry<AppRoutes.ListFoldersScreen> {
                val viewModel: ListFoldersViewModel = viewModel()

                ListFoldersRoute(
                    viewModel = viewModel,
                    onCreateFolderClick = {
                        backStack.add(AppRoutes.CreateFilesPropertiesScreen(FileFormMode.CreatingFolder))
                    },
                    onFolderOpen = {
                        backStack.add(AppRoutes.EntryGuidesScreen)
                    },
                    onNavWithoutFolders = {
                        if (backStack.isNotEmpty()) {
                            backStack[backStack.lastIndex] = AppRoutes.WithoutFoldersScreen
                        }
                    },
                    onRenameFolderClick = { folder ->
                        backStack.add(
                            AppRoutes.CreateFilesPropertiesScreen(
                                FileFormMode.RenameFolder(folder)
                            )
                        )
                    }
                )
            }

            entry<AppRoutes.EntryGuidesScreen> {
                val viewModel: FragReviewEntryViewModel = viewModel()

                GuidesEntryRoute(
                    viewModel = viewModel,
                    onNavigateListGuidesRoute = {
                        if (backStack.isNotEmpty()) {
                            backStack[backStack.lastIndex] = AppRoutes.ListGuidesScreen
                        }
                    },
                    onNavigateWithoutFilesScreen = {
                        if (backStack.isNotEmpty()) {
                            backStack[backStack.lastIndex] = AppRoutes.WithoutGuidesScreen
                        }
                    },
                )
            }

            entry<AppRoutes.WithoutGuidesScreen> {
                val viewModel: FragmentWithoutFilesViewModel = viewModel()

                WithoutFilesRoute(
                    viewModel = viewModel,
                    onAddGuideClick = {
                        backStack.add(AppRoutes.CreateFilesPropertiesScreen(FileFormMode.CreatingFile))
                    },
                    onNavListGuides = {
                        if (backStack.isNotEmpty()) {
                            backStack[backStack.lastIndex] = AppRoutes.ListGuidesScreen
                        }
                    },
                    onBackNav = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<AppRoutes.ListGuidesScreen> {
                val viewModel: FragmentListGuidesViewModel = viewModel()

                ListGuidesRoute(
                    viewModel = viewModel,
                    onAddGuideClick = {
                        backStack.add(AppRoutes.CreateFilesPropertiesScreen(FileFormMode.CreatingFile))
                    },
                    onOpenGuideClick = {
                        backStack.add(AppRoutes.PreviewQuestionsScreen)
                    },
                    onRenameGuideClick = { guideUIModel ->
                        backStack.add(
                            AppRoutes.CreateFilesPropertiesScreen(
                                FileFormMode.RenameFile(guideUIModel)
                            )
                        )
                    },
                    onMoveGuideClick = {
                        backStack.clear()
                        backStack.add(AppRoutes.ListFoldersScreen)
                    },
                    onNavWithoutGuides = {
                        if (backStack.isNotEmpty()) {
                            backStack[backStack.lastIndex] = AppRoutes.WithoutGuidesScreen
                        }
                    }
                )
            }

            entry<AppRoutes.CreateFilesPropertiesScreen> { mode ->
                val viewModel: CreateFilesViewModel = viewModel()

                CreateFilesPropertiesRoute(
                    viewModel = viewModel,
                    fileFormMode = mode.fileFormMode,
                    onRenameFile = {
                        backStack.removeLastOrNull()
                    },
                    onNavFillingGuide = {
                        if (backStack.isNotEmpty()) {
                            backStack[backStack.lastIndex] = AppRoutes.StudyGuideScreen
                        }
                    },
                    onCreateFolder = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<AppRoutes.StudyGuideScreen> {
                StudyGuideRoute(
                    viewModel = viewModelSharedCreateFile,
                    onOpenAssetClick = { typeContent, posItem ->
                        when (typeContent) {
                            is QuestionContentUi.Image -> {
                                backStack.add(
                                    AppRoutes.CreateImageScreen(
                                        questionContentMode = QuestionContentMode.EDITING,
                                        posItem = posItem
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
                                        posItem = posItem
                                    )
                                )
                            }
                        }
                    },
                    onAddAssetClick = { mediaSelected, posItem ->
                        when (mediaSelected) {
                            ContentType.TEXT ->
                                backStack.add(
                                    AppRoutes.CreateTextScreen(
                                        questionContentMode = QuestionContentMode.CREATING,
                                        posItem = posItem
                                    )
                                )

                            ContentType.IMAGE -> {
                                backStack.add(
                                    AppRoutes.CreateImageScreen(
                                        questionContentMode = QuestionContentMode.CREATING,
                                        posItem = posItem
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
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<AppRoutes.CreateImageScreen> { values ->
                CreateImageRoute(
                    questionContentMode = values.questionContentMode,
                    posItem = values.posItem,
                    viewModel = viewModelSharedCreateFile,
                    onBackNav = { backStack.removeLastOrNull() }
                )
            }

            entry<AppRoutes.CreateTextScreen> { values ->
                CreateTextRoute(
                    questionContentMode = values.questionContentMode,
                    viewModel = viewModelSharedCreateFile,
                    posItem = values.posItem,
                    onSaveText = { backStack.removeLastOrNull() },
                    onBackNav = { backStack.removeLastOrNull() }
                )
            }

            entry<AppRoutes.PreviewQuestionsScreen> {
                val viewModel: PreviewViewModel = viewModel()

                PreviewQuestionsRoute(
                    viewModel = viewModel,
                    onEditingGuideClick = {
                        backStack.add(AppRoutes.StudyGuideScreen)
                    },
                    onPlayGuideClick = {
                        backStack.add(AppRoutes.StudyGuideScreen)
                    },
                    onBackNav = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}
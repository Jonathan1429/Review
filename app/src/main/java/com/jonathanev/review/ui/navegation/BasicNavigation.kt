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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jonathanev.review.presentation.model.FileFormMode
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
import com.jonathanev.review.presentation.viewmodel.WithoutFoldersViewModel
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
import com.jonathanev.review.ui.screens.WithoutFoldersRoute
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun BasicNavigation() {
    val backStack = rememberNavBackStack(AppRoutes.MainScreen)

    val context = LocalContext.current

    // Estado y efecto para el aviso de doble toque
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

    backStack.forEachIndexed { index, key ->
        //Log.i("BACKSTACK", "[$index]: $key")
        println("BACKSTACK [$index]: $key")
    }

    //Log.i("BACKSTACK", "========================================")
    println("BACKSTACK: ========================================")

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
            val viewModelSharedCreateFile: SharedFragmentCreateFileViewModel = viewModel()

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
                    }
                )
            }
            entry<AppRoutes.EntryGuidesScreen> {
                val viewModel: FragReviewEntryViewModel = viewModel()

                GuidesEntryRoute(
                    viewModel = viewModel,
                    onNavigateListGuidesRoute = {
                        if (backStack.isNotEmpty()) {
                            backStack[backStack.lastIndex] =
                                AppRoutes.ListGuidesScreen
                        }
                    },
                    onNavigateWithoutFilesScreen = {
                        if (backStack.isNotEmpty()) {
                            backStack[backStack.lastIndex] =
                                AppRoutes.WithoutGuidesScreen
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
                                FileFormMode.RenameFile(
                                    guideUIModel
                                )
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
                        backStack.removeLastOrNull()
                        backStack.add(
                            AppRoutes.FillingGuideScreen(GuideMode.Create)
                        )
                    },
                    onCreateFolder = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<AppRoutes.FillingGuideScreen> { action ->
                val context = LocalContext.current

                FillingGuideRoute(
                    viewModel = viewModelSharedCreateFile,
                    guideMode = action.guideMode,
                    onOpenAssetClick = { typeContent, posItem ->
                        when (typeContent) {
                            is QuestionContentUi.Image -> {
                                backStack.add(
                                    AppRoutes.CreateImageScreen(
                                        questionContentMode = QuestionContentMode.EDITING,
                                        posItem = posItem,
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
                                        posItem = posItem,
                                        guideMode = action.guideMode
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
                                        posItem = posItem,
                                        guideMode = action.guideMode
                                    )
                                )

                            ContentType.IMAGE -> {
                                backStack.add(
                                    AppRoutes.CreateImageScreen(
                                        questionContentMode = QuestionContentMode.CREATING,
                                        posItem = posItem,
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
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<AppRoutes.CreateImageScreen> { values ->
                CreateImageRoute(
                    questionContentMode = values.questionContentMode,
                    guideMode = values.guideMode,
                    posItem = values.posItem,
                    viewModel = viewModelSharedCreateFile,
                    onBackNav = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<AppRoutes.CreateTextScreen> { values ->
                CreateTextRoute(
                    questionContentMode = values.questionContentMode,
                    guideMode = values.guideMode,
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
                        backStack.add(AppRoutes.FillingGuideScreen(GuideMode.Edit))
                    },
                    onPlayGuideClick = {
                        backStack.add(
                            AppRoutes.FillingGuideScreen(GuideMode.Review)
                        )
                    }
                )
            }
        }
    )
}
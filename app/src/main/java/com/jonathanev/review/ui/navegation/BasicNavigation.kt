package com.jonathanev.review.ui.navegation

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.presentation.event.MainUiEvent
import com.jonathanev.review.presentation.model.FileFormMode
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.model.GuideMode
import com.jonathanev.review.presentation.model.GuideUiModel
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.viewmodel.CreateFilesViewModel
import com.jonathanev.review.presentation.viewmodel.FragmentListGuidesViewModel
import com.jonathanev.review.presentation.viewmodel.FragmentRepasarViewModel
import com.jonathanev.review.presentation.viewmodel.MainActivityViewModel
import com.jonathanev.review.presentation.viewmodel.NavigationViewModel
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.ui.screens.CreateFilesPropertiesRoute
import com.jonathanev.review.ui.screens.CreateImageRoute
import com.jonathanev.review.ui.screens.CreateTextRoute
import com.jonathanev.review.ui.screens.FillingGuideRoute
import com.jonathanev.review.ui.screens.ListFoldersScreen
import com.jonathanev.review.ui.screens.ListGuidesRoute
import com.jonathanev.review.ui.screens.PreviewQuestionsRoute
import com.jonathanev.review.ui.screens.WithoutFilesScreen
import com.jonathanev.review.ui.screens.WithoutFoldersScreen

@Composable
fun BasicNavigation() {
    val backStack = rememberNavBackStack(AppRoutes.MainScreen())

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
            val viewModel: MainActivityViewModel = viewModel()
            val navigationViewModel: NavigationViewModel = viewModel()
            val context = LocalContext.current

            val folders = viewModel.folders.collectAsStateWithLifecycle().value

            entry<AppRoutes.MainScreen> { mode ->
                LaunchedEffect(Unit) {
                    viewModel.createFolders()
                    viewModel.getAllFolders()

                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            MainUiEvent.ShowCreateFoldersError -> {
                                AlertDialog.Builder(context).apply {
                                    setTitle("Error")
                                    setMessage("No se pudieron crear los ficheros correctamente")
                                    setCancelable(false)
                                    setPositiveButton("Reintentar") { dialog, _ ->
                                        viewModel.createFolders()
                                        dialog.dismiss()
                                    }
                                    setNegativeButton("Cancelar") { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                }.create().show()
                            }
                        }
                    }
                }

                if (folders.isEmpty()) {
                    WithoutFoldersScreen(
                        onNavCreateFilesProperties = {
                            backStack.add(AppRoutes.CreateFilesPropertiesScreen(FileFormMode.CreatingFolder))
                        }
                    )
                } else {
                    ListFoldersScreen(
                        guias = folders,
                        onCreateFolderClick = {
                            backStack.add(AppRoutes.CreateFilesPropertiesScreen(FileFormMode.CreatingFolder))
                        },
                        onFolderClick = { nameGuide, folderAction ->
                            navigationViewModel.next(nameGuide)
                            backStack.add(AppRoutes.ListGuidesScreen(folderAction))
                        },
                        fileInteractionMode = mode.fileInteractionMode
                    )
                }
            }
            entry<AppRoutes.ListGuidesScreen> { action ->
                val viewModel: FragmentListGuidesViewModel = viewModel()
                val navigationViewModel: NavigationViewModel = viewModel()
                val relativeGuidePath =
                    RelativeGuidePath(navigationViewModel.relativeGuidePath.collectAsStateWithLifecycle().value)
                val guides = viewModel.guides.collectAsStateWithLifecycle().value

                LaunchedEffect(Unit) {
                    viewModel.getAllGuides(relativeGuidePath)
                }

                if (guides.isEmpty()) {
                    WithoutFilesScreen(
                        onAddGuideClick = {
                            backStack.add(AppRoutes.CreateFilesPropertiesScreen(FileFormMode.CreatingFile))
                        }
                    )
                } else {
                    ListGuidesRoute(
                        viewModel = viewModel,
                        navigationViewModel = navigationViewModel,
                        guides = guides,
                        fileInteractionMode = action.fileInteractionMode,
                        onAddGuideClick = {
                            backStack.add(AppRoutes.CreateFilesPropertiesScreen(FileFormMode.CreatingFile))
                        },
                        onOpenGuideClick = { nameGuide ->
                            backStack.add(AppRoutes.PreviewQuestionsScreen(nameGuide))
                        },
                        onDeleteGuideClick = {
                            backStack.removeLastOrNull()
                        },
                        onRenameGuideClick = { propertiesGuide ->
                            backStack.add(
                                AppRoutes.CreateFilesPropertiesScreen(
                                    FileFormMode.RenameFile(
                                        GuideUiModel(
                                            nameGuide = propertiesGuide.name,
                                            description = propertiesGuide.description
                                        )
                                    )
                                )
                            )
                        },
                        onMoveGuideClick = {
                            navigationViewModel.setMainPath()
                            backStack.clear()
                            backStack.add(AppRoutes.MainScreen(FileInteractionMode.MovingItem))
                        },
                        onMoveCancelGuideClick = {
                            navigationViewModel.setMainPath()
                            backStack.clear()
                            backStack.add(AppRoutes.MainScreen(FileInteractionMode.Default))
                        },
                        onMoveSuccessGuideClick = {
                            navigationViewModel.setMainPath()
                            backStack.clear()
                            backStack.add(AppRoutes.MainScreen(FileInteractionMode.Default))
                        },
                        onBackNav = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
            }
            entry<AppRoutes.CreateFilesPropertiesScreen> { mode ->
                val viewModel: CreateFilesViewModel = viewModel()
                val viewModelNavigation: NavigationViewModel = viewModel()

                LaunchedEffect(Unit) {
                    when (mode.fileFormMode) {
                        FileFormMode.CreatingFile -> viewModel.initWithMode(mode.fileFormMode)
                        is FileFormMode.RenameFile -> {
                            viewModel.initWithMode(mode.fileFormMode)

                            val oldName = mode.fileFormMode.guideUiModel.nameGuide
                            val responseFillFields = viewModel.fillFields(oldName)
                            if (!responseFillFields) {
                                Toast.makeText(
                                    context,
                                    "Guia dañada, imposible renombrar",
                                    Toast.LENGTH_SHORT
                                ).show()
                                backStack.removeLastOrNull()
                            }
                        }

                        FileFormMode.CreatingFolder -> viewModel.initWithMode(mode.fileFormMode)
                    }
                }

                CreateFilesPropertiesRoute(
                    viewModel = viewModel,
                    viewModelNavigation = viewModelNavigation,
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
                val viewModel: SharedFragmentCreateFileViewModel = viewModel()
                val viewModelNavigation: NavigationViewModel = viewModel()
                val relativeGuidePath =
                    viewModelNavigation.relativeGuidePath.collectAsStateWithLifecycle().value

                FillingGuideRoute(
                    viewModel = viewModel,
                    guideMode = action.guideMode,
                    relativeGuidePath = RelativeGuidePath(value = relativeGuidePath),
                    onAssetClick = { typeContent ->
                        when (typeContent) {
                            is QuestionContentUi.Image -> {
                                backStack.add(
                                    AppRoutes.CreateImageScreen(
                                        QuestionContentUi.Image(
                                            typeContent.uri,
                                            typeContent.nameFile
                                        ),
                                        action.guideMode
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
                                        QuestionContentUi.Text(
                                            typeContent.text,
                                            typeContent.colorRanges
                                        ),
                                        action.guideMode
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
                        backStack.add(AppRoutes.MainScreen(FileInteractionMode.Default))
                    }
                )
            }
            entry<AppRoutes.CreateImageScreen> { imageContent ->
                val viewModel: SharedFragmentCreateFileViewModel = viewModel()

                CreateImageRoute(
                    guideMode = imageContent.guideMode,
                    contentType = imageContent.contentType,
                    viewModel = viewModel,
                    imageUploaded = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<AppRoutes.CreateTextScreen> { textContent ->
                val viewModel: SharedFragmentCreateFileViewModel = viewModel()

                CreateTextRoute(
                    guideMode = textContent.guideMode,
                    viewModel = viewModel,
                    contentType = textContent.contentType,
                    onSaveText = { backStack.removeLastOrNull() })
            }

            entry<AppRoutes.PreviewQuestionsScreen> { value ->
                val viewModel: FragmentRepasarViewModel = viewModel()
                val navigationViewModel: NavigationViewModel = viewModel()

                val propertiesGuide = viewModel.uiState.collectAsStateWithLifecycle().value

                PreviewQuestionsRoute(
                    viewModel = viewModel,
                    navigationViewModel = navigationViewModel,
                    nameGuide = value.nameGuide,
                    onEditingGuideClick = { position ->
                        backStack.add(
                            AppRoutes.FillingGuideScreen(
                                GuideMode.Edit(
                                    propertiesGuide.fileName,
                                    propertiesGuide.description,
                                    position
                                )
                            )
                        )
                    },
                    onPlayGuideClick = {
                        backStack.add(
                            AppRoutes.FillingGuideScreen(
                                GuideMode.Review(
                                    nameGuide = propertiesGuide.fileName,
                                    posQuestion = 0
                                )
                            )
                        )
                    }
                )
            }
        }
    )
}
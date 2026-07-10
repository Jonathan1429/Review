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
import com.jonathanev.review.presentation.model.ActionGuide
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.viewmodel.CreateFilesViewModel
import com.jonathanev.review.presentation.viewmodel.FragmentListGuidesViewModel
import com.jonathanev.review.presentation.viewmodel.FragmentRepasarViewModel
import com.jonathanev.review.presentation.viewmodel.MainActivityViewModel
import com.jonathanev.review.presentation.viewmodel.NavigationViewModel
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.ui.screens.CreateFilesPropertiesRoute
import com.jonathanev.review.ui.screens.CreateImageRoute
import com.jonathanev.review.ui.screens.FillingGuideRoute
import com.jonathanev.review.ui.screens.ListFoldersScreen
import com.jonathanev.review.ui.screens.ListGuidesRoute
import com.jonathanev.review.ui.screens.PreviewQuestionsRoute
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

            entry<AppRoutes.MainScreen> { action ->
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
                        onNavCreateFilesProperties = { typeAction ->
                            backStack.add(AppRoutes.CreateFilesPropertiesScreen(typeAction))
                        }
                    )
                } else {
                    ListFoldersScreen(
                        guias = folders,
                        onCreateFolderClick = { typeAction ->
                            backStack.add(AppRoutes.CreateFilesPropertiesScreen(typeAction))
                        },
                        onFolderClick = { nameGuide, folderAction ->
                            navigationViewModel.next(nameGuide)
                            backStack.add(AppRoutes.ListGuidesScreen(folderAction))
                        },
                        folderAction = action.folderAction
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

                ListGuidesRoute(
                    viewModel = viewModel,
                    navigationViewModel = navigationViewModel,
                    guides = guides,
                    folderAction = action.folderAction,
                    onAddGuideClick = {
                        backStack.add(AppRoutes.CreateFilesPropertiesScreen(FolderAction.CreatingFile))
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
                                FolderAction.RenamingFile(
                                    fileName = propertiesGuide.name,
                                    description = propertiesGuide.description
                                )
                            )
                        )
                    },
                    onMoveGuideClick = { folderAction ->
                        navigationViewModel.setMainPath()
                        backStack.clear()
                        backStack.add(AppRoutes.MainScreen(folderAction))
                    },
                    onMoveCancelGuideClick = {
                        navigationViewModel.setMainPath()
                        backStack.clear()
                        backStack.add(AppRoutes.MainScreen(FolderAction.None))
                    },
                    onMoveSuccessGuideClick = {
                        navigationViewModel.setMainPath()
                        backStack.clear()
                        backStack.add(AppRoutes.MainScreen(FolderAction.None))
                    },
                    onBackNav = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<AppRoutes.CreateFilesPropertiesScreen> { typeAction ->
                val viewModel: CreateFilesViewModel = viewModel()
                val viewModelNavigation: NavigationViewModel = viewModel()

                LaunchedEffect(Unit) {
                    when (typeAction.folderAction) {
                        FolderAction.CreatingFile -> {
                            viewModel.initWithMode(typeAction.folderAction)
                        }

                        FolderAction.CreatingFolder -> {
                            viewModel.initWithMode(typeAction.folderAction)
                        }

                        is FolderAction.RenamingFile -> {
                            viewModel.initWithMode(typeAction.folderAction)

                            val oldName = typeAction.folderAction.fileName
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

                        FolderAction.RenamingFolder -> {
                            Toast.makeText(
                                context,
                                "No se encuentra disponible el renombrar un folder",
                                Toast.LENGTH_SHORT
                            ).show()
                            backStack.removeLastOrNull()
                        }

                        FolderAction.MovingFile, FolderAction.None -> {
                            Toast.makeText(
                                context,
                                "No se puede procesar la solicitud",
                                Toast.LENGTH_SHORT
                            ).show()
                            backStack.removeLastOrNull()
                        }
                    }
                }

                CreateFilesPropertiesRoute(
                    viewModel = viewModel,
                    viewModelNavigation = viewModelNavigation,
                    mode = typeAction.folderAction,
                    onNavBack = { backStack.removeLastOrNull() },
                    onNavFillingGuide = { propertiesGuide ->
                        backStack.add(
                            AppRoutes.FillingGuideScreen(
                                ActionGuide.CREATE(
                                    propertiesGuide.name,
                                    propertiesGuide.description
                                )
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
                    action = action.actionGuide,
                    relativeGuidePath = RelativeGuidePath(value = relativeGuidePath),
                    onModifyAssetClick = { typeContent ->
                        when (typeContent) {
                            is QuestionContentUi.Image -> {
                                backStack.add(
                                    AppRoutes.CreateImageScreen(
                                        QuestionContentUi.Image(
                                            typeContent.uri,
                                            typeContent.nameFile
                                        )
                                    )
                                )
                            }

                            QuestionContentUi.None -> TODO()
                            is QuestionContentUi.Text -> {
                                backStack.add(
                                    AppRoutes.CreateTextScreen(
                                        QuestionContentUi.Text(
                                            typeContent.text,
                                            typeContent.colorRanges
                                        )
                                    )
                                )
                            }
                        }
                    }
                )
            }
            entry<AppRoutes.CreateImageScreen> { imageContent ->
                val viewModel: SharedFragmentCreateFileViewModel = viewModel()

                CreateImageRoute(
                    contentType = imageContent.contentType,
                    viewModel = viewModel,
                    imageUploaded = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<AppRoutes.CreateTextScreen> { textContent ->

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
                                ActionGuide.EDIT(
                                    propertiesGuide.fileName,
                                    propertiesGuide.description,
                                    position
                                )
                            )
                        )
                    }
                )
            }
            /*entry<AppRoutes.NotificationScreen> {
                NotificationScreen(
                    navigateUp = {
                        backStack.removeLastOrNull()
                    },
                    onNavPrevisualizacion = { uuid ->
                        backStack.add(AppRoutes.PrevisualizacionScreen(uuid))
                    }
                )
            }
            entry<AppRoutes.PrevisualizacionScreen> { key ->
                PrevisualizacionScreen(
                    uuidReporte = key.uuid,
                    navigateUp = {
                        backStack.removeLastOrNull()
                    }
                )
            }*/
        }
    )
}
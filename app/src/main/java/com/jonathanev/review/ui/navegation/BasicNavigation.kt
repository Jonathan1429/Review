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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jonathanev.review.presentation.event.MainUiEvent
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.presentation.model.GuideResultUi
import com.jonathanev.review.presentation.state.CreatingUIState.Message
import com.jonathanev.review.presentation.viewmodel.CreateFilesViewModel
import com.jonathanev.review.presentation.viewmodel.MainActivityViewModel
import com.jonathanev.review.presentation.viewmodel.NavigationViewModel
import com.jonathanev.review.ui.model.PropertiesGuide
import com.jonathanev.review.ui.screens.CreateFilesPropertiesRoute
import com.jonathanev.review.ui.screens.FillingGuideScreen
import com.jonathanev.review.ui.screens.MainScreen

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
            val viewModel: MainActivityViewModel = viewModel()
            val context = LocalContext.current

            entry<AppRoutes.MainScreen> {
                LaunchedEffect(Unit) {
                    viewModel.createFolders()

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

                MainScreen(
                    onNavCreateFilesProperties = { typeAction ->
                        backStack.add(AppRoutes.CreateFilesPropertiesScreen(typeAction))
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
                            if (!responseFillFields){
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
                        backStack.add(AppRoutes.FillingGuideScreen(propertiesGuide))
                    }
                )
            }
            entry<AppRoutes.FillingGuideScreen> { propertiesGuide ->
                FillingGuideScreen()
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
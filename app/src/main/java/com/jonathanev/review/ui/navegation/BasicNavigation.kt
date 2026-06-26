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
import com.jonathanev.review.presentation.viewmodel.CreateFilesViewModel
import com.jonathanev.review.presentation.viewmodel.MainActivityViewModel
import com.jonathanev.review.ui.screens.CreateFilesPropertiesRoute
import com.jonathanev.review.ui.screens.CreateFilesPropertiesScreen
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
                        when(event){
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

                CreateFilesPropertiesRoute(
                    viewModel = viewModel,
                    mode = typeAction.folderAction
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
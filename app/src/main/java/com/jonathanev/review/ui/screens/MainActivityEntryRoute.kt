package com.jonathanev.review.ui.screens

import android.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.presentation.event.MainUiEvent
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.presentation.viewmodel.MainActivityViewModel

@Composable
fun MainActivityEntryRoute(
    viewModel: MainActivityViewModel,
    onNavWithoutFolderScreen: () -> Unit,
    onNavListFoldersScreen: (List<FolderUiModel>) -> Unit
) {
    val context = LocalContext.current

    val folders = viewModel.folders.collectAsStateWithLifecycle().value
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
        onNavWithoutFolderScreen()
    } else {
        onNavListFoldersScreen(folders)
    }
}